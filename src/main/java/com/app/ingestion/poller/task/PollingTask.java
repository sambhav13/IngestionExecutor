package com.app.ingestion.poller.task;

import com.app.ingestion.archiver.Archiver;
import com.app.ingestion.exception.IngestionException;
import com.app.ingestion.fileselector.FileSelectorType;
import com.app.ingestion.model.*;
import com.app.ingestion.nfs.NFSUtil;
import com.app.ingestion.poller.AbstractPollingTask;
import com.app.ingestion.poller.DirSftpPoller;
import com.app.ingestion.sftp.ISftpUtil;
import com.app.ingestion.sftp.SftpFactoryManager;
import com.app.ingestion.util.ConfigUtil;
import com.app.ingestion.util.ExponentialBackOff;
import com.app.ingestion.validator.design.Criterion;
import com.app.ingestion.validator.design.Validator;
import com.app.ingestion.validator.design.impl.RegexCriterion;
import com.app.ingestion.validator.design.impl.RegexValidator;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.Configuration;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

import static com.app.ingestion.util.SftpConstants.*;
import static com.app.ingestion.util.SourceConstants.RETRY_ATTEMPT;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class PollingTask extends AbstractPollingTask  implements Callable<String> {

    private JschConnection jschConnection;
    private static Logger LOG = LoggerFactory.getLogger(PollingTask.class);

    private boolean isMaster;
    private String pattern;
    private String hdfsLocation;
    private String archiveLocation;
    private TaskMetaData taskMetaData;
    private Archiver archiver;


    //parent properties
    private String dirId;
    private String dirPath;
    private String sourceName;
    private String assetClass;
    private String topic = null;
    private String errorTopic = null;
    private boolean useChecksum;
    private boolean useFilePermission;

    private String sftpHost;
    private String sftpUser;
    private String sftpPort;
    private String sftpKeyLocation;
    private ExponentialBackOff backOff;
    private ExponentialBackOff noFilesbackOff;
    private ExponentialBackOff throwableBackOff;


    private int retryTimeGap;
    private int backOffStep;
    private int backOffCap;
    private int retryAttemp;
    private String successNotificationTopic;

    private DirSftpPoller poller;
    private Validator validator;



    public static final String STAMP_ERROR_NOTIFICATION_FAILURE = ".error_notification_failure";
    public static final String STAMP_SUCCESS_NOTIFICATION_FAILURE = ".success_notification_failure";

    private Map<String,String> props;

    public PollingTask(){

    }
    public PollingTask(JschConnection jschConnection, Map<String,String> props) {
        this.props = props;
        this.jschConnection = jschConnection;
        this.dirId = props.get(AC_TASK_DIR_ID);
        //this.topic = props.get(INGEST_TRANSFER_TOPIC);
        this.useChecksum = Boolean.parseBoolean(props.getOrDefault(CHECKSUM_FLAG, "false"));
        LOG.info("Checksum flag is " + useChecksum);
        this.useFilePermission = Boolean.parseBoolean(props.getOrDefault(FILE_READ_PERMISSION_FLAG, "false"));
        LOG.info("File permissions flag is " + useFilePermission);

        backOffStep = Integer.parseInt(props.getOrDefault(BACK_OFF_STEP, "10"));
        backOffCap = Integer.parseInt(props.getOrDefault(BACK_OFF_CAP, "30"));
        retryTimeGap = Integer.parseInt(props.getOrDefault(RETRY_TIME_GAP, "4"));
        retryAttemp = Integer.parseInt(props.getOrDefault(RETRY_ATTEMPT, "3"));
        backOff = new ExponentialBackOff(Duration.ofSeconds(backOffStep), Duration.ofSeconds(backOffCap));
        noFilesbackOff = new ExponentialBackOff(Duration.ofSeconds(backOffStep), Duration.ofSeconds(backOffCap));
        throwableBackOff = new ExponentialBackOff(Duration.ofSeconds(backOffStep), Duration.ofSeconds(backOffCap));


        this.sftpUser = props.get(AC_SFTP_SERVER_INPUT_USER);
        this.sftpHost = props.get(AC_SFTP_SERVER_INPUT_HOST);
        this.sftpPort = props.getOrDefault(AC_SFTP_SERVER_INPUT_PORT,"22");
        this.sftpKeyLocation = props.get(AC_SFTP_SERVER_INPUT_KEY_LOCATION);

        props.put(AC_SFTP_SERVER_INPUT_HOST, sftpHost);

        DirectoryMetaData dirMetaData = ConfigUtil.getConfiguredDirForTask(dirId, props);

        /*if(props.getOrDefault(AUTHENTICATION_METHOD,"").equalsIgnoreCase(AUTHENTICATION_METHOD_KERBEROS)){
            try{
                Configuration hadoopConf = new Configuration();
                UserGroupInformation.setConfiguration(hadoopConf);
                UserGroupInformation.loginUserFromKeytabl(props.get(KERBEROS_PRINCIPAL),props.get(KEYTAB_FILE));
            }catch(IOException e){
                e.printStackTrace();
            }
        }*/

        taskMetaData = ConfigUtil.prepareTaskMetaData(dirMetaData, props, isMaster, pattern, useChecksum);
        LOG.info("Master Task...." + taskMetaData.isMaster());

        dirPath = taskMetaData.getDir();
        sourceName = taskMetaData.getSourceName();
        assetClass = dirMetaData.getAssetClass();
        Criterion criteriaInTransferFile = new RegexCriterion();
        criteriaInTransferFile.addNegativeCriteria(taskMetaData.getIgnoredPatterns());
        ISftpUtil sftpUtil = SftpFactoryManager.getInstance();
        try{
           this.jschConnection  = sftpUtil.tryToEstablishConnection(props, sftpHost, Integer.parseInt(sftpPort),
                sftpUser, sftpKeyLocation);
        }catch(Exception e){
            LOG.info("Could not establish sftp connection - start method");
        }


        //we will create a poller object anyway - if the connection is null then inner JschPoller
        // will be re created without connection
        poller =  new DirSftpPoller(taskMetaData.getDir(), FileSelectorType.FILENAME,new RegexValidator(criteriaInTransferFile),
                props,jschConnection);

        poller.initializePoller();
        Criterion criteria = new RegexCriterion();
        criteria.addPositiveCriteria(taskMetaData.getAllowedPatterns());
        criteria.addNegativeCriteria(taskMetaData.getNotAllowedPatterns());


        validator = new RegexValidator(criteria);
        this.hdfsLocation = props.get(HDFS_TARGET_LOCATION);
        this.archiveLocation = props.get(ARCHIVE_LOCATION);
        this.archiver = new Archiver(props);



    }
    @Override
    public String call() throws Exception {
        while(true) {
            poll();
        }
        //return Thread.currentThread().getName();
    }

    public List<Record> poll() throws InterruptedException{

        LOG.debug("start poll");
        try{
            String threadName  = this.getClass().getCanonicalName() +  "---" +Thread.currentThread().getName();
            if((this.jschConnection == null) || !this.jschConnection.isValid()){
                //if not valid try to re-establish
                LOG.info("poll - trying to establish connection");
                ISftpUtil sftpUtil = SftpFactoryManager.getInstance();
                JschConnection connection =  sftpUtil.tryToEstablishConnection(props,sftpHost,
                        Integer.parseInt(sftpPort),sftpUser,sftpKeyLocation);

                this.jschConnection = jschConnection;
                this.poller.setJschConnection(jschConnection);

                this.poller.initializePoller();
                LOG.info("Connection established successfully");
            }

            List<FileMetaData> fileList = poller.poll();
            Map<FileMetaData,Boolean> validationResultMap = validator.isValid(fileList);
            handleInvalidFiles(validationResultMap);

            List<FileMetaData> validFiles = filterValidationResults(validationResultMap,true);

            if(useFilePermission){
                List<FileMetaData> nonPermissiveFilesList = filterReadPermission(validFiles,false);
                LOG.debug("Number of files doesn't have read [permission in current poll "+ nonPermissiveFilesList.size());
               // notifyInvalidFiles(nonPermissiveFilesList,Not);
                validFiles = filterReadPermission(validFiles,true);
            }

            if(!validFiles.isEmpty()) {
                noFilesbackOff.resetIteration();
                LOG.info("Number of valid files in current poll" + validFiles.size());
                if (useChecksum) {
                    //commented for now
                    validFiles = poller.setMd5Checksum(validFiles);
                }
                return transferValidFiles(validFiles);
            }else{
                noFilesbackOff = noFilesbackOff.nextRun();
                LOG.info(dirPath + "FOR SFTP HOST: "+sftpHost+" ASSET CLASS: "+ assetClass+ " SourceName: "+sourceName+", No valid files in curren poll, resetting no-files Backoff, " +
                        "BACKOFF REMAINING" +noFilesbackOff.remaining().getSeconds());
            }

            while(!noFilesbackOff.passed()){
                LOG.debug("FOR THREAD: "+ Thread.currentThread().getName()+" SFTP HOST: "+sftpHost+" ASSET CLASS: "+ assetClass+ " SourceName: "+sourceName+", No Files, backoff remaining "+ noFilesbackOff.remaining().getSeconds());
                Thread.sleep(5000);
            }

        }catch(Throwable th){

            LOG.error("The Throwable class is "+th.getClass().toString());
            LOG.error("The Exception Message is "+th.getMessage());
            throwableBackOff = throwableBackOff.nextRun();
        }
        return new ArrayList<>();
    }

    @Override
    protected List<Record> transferValidFiles(List<FileMetaData> fileList) throws IOException, InterruptedException {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).format(new Date()).toString();
        for (FileMetaData fileMetaData : fileList) {

            String destPath = "";
            String destDirPath;
            destDirPath = NFSUtil.getTargetDir(hdfsLocation, taskMetaData.getSourceName(), taskMetaData.getAssetClass(), date);
            destPath = NFSUtil.getTargetDestPath(destDirPath, fileMetaData.getFileName());

            try {
                NFSUtil.saveFileToNFS(jschConnection, dirPath, fileMetaData, destDirPath, useChecksum, retryAttemp, retryTimeGap);

                //notifyFileTransfer
                //notifyErros
                //archive File
                try {
                    archiveFile(fileMetaData.getFileName(), date, Archiver.TRANSFERRED);
                }catch(Exception ingE1){
                    ingE1.printStackTrace();
                    String message = NFSUtil.removeHdfsJunkFile(destPath,jschConnection);
                    stampFile(STAMP_SUCCESS_NOTIFICATION_FAILURE, taskMetaData.getDir(), fileMetaData.getFileName());
                }
            } catch (IngestionException ingE2) {
            //catch (IngestionException ingE2) {
                //notifyNotificationFailure
                //System.out.println("h");
                try {
                    archiveFile(fileMetaData.getFileName(),date,Archiver.ERROR);
                    stampFile(STAMP_ERROR_NOTIFICATION_FAILURE, taskMetaData.getDir(), taskMetaData.getDir());
                }catch(Exception ingE3){
                    stampFile(STAMP_ERROR_NOTIFICATION_FAILURE,taskMetaData.getDir(),taskMetaData.getDir());
                }
            }

        }
        throwableBackOff.resetIteration();;
        return null;
    }

    public void stampFile(String stamp,String dir,String fileName){
        String stampedFileName = fileName + stamp;
        try{
            LOG.info("Stamping file: "+dir+ FILE_SEPARATOR+ fileName +" =====> into: "+dir+FILE_SEPARATOR+stampedFileName);
            jschConnection.getChannelSftp().rename(dir+FILE_SEPARATOR+fileName,dir+FILE_SEPARATOR+stampedFileName);

        }catch (SftpException e){
            LOG.info("Could not stamp file "+ dir + FILE_SEPARATOR+fileName+" with stamp "+stamp);
            LOG.error(e.getMessage());
        }
    }

    @Override
    protected List<FileMetaData> filterOldFiles(List<FileMetaData> fileList) {
        return fileList;
    }

    @Override
    protected void handleInvalidFiles(Map<FileMetaData, Boolean> validationResultMap) {

        if(isMaster){
            List<FileMetaData> invalidFiles  = filterValidationResults(validationResultMap,false);
            if(invalidFiles != null && !invalidFiles.isEmpty()){
                LOG.info("Number of Invalid files found in current poll "+invalidFiles.size());
                String date = new SimpleDateFormat("yyyyMMdd",Locale.ENGLISH).format(new Date().toString());
                for(FileMetaData invalidFile:invalidFiles){
                    LOG.debug("Archiving file: "+invalidFile.getFileName());
                    archiveFile(invalidFile.getFileName(),date, Archiver.ERROR);
                }
            }
        }
    }


    private void archiveFile(String fileName,String date,String status){

        LOG.debug("Archiving file {} ",fileName);
        boolean archived = this.archiver.archive(jschConnection,fileName,taskMetaData.getAssetClass(),taskMetaData.getSourceName(),
                taskMetaData.getDir(),date,status,archiver.PERMISSION,this.archiveLocation);
        if(!archived){
            String stamp = archiver.STAMP_NOT_TRANSFERRED_NOT_ARCHIVED;
            if(status.equals(archiver.TRANSFERRED)){
                stamp = archiver.STAMP_TRANSFERRED_NOT_ARCHIVED;
            }
            stampFile(stamp,taskMetaData.getDir(),fileName);
        }
    }
    @Override
    protected List<Record> createSourceRecords(List<FileMetaData> metaDataList) {
        return null;
    }
}
