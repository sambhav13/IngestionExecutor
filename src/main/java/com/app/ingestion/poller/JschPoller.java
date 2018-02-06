package com.app.ingestion.poller;

import com.app.ingestion.model.FileMetaData;
import com.app.ingestion.model.JschConnection;
import com.app.ingestion.poller.jsch.JschChannelExec;
import com.app.ingestion.sftp.ISftpUtil;
import com.app.ingestion.sftp.SftpFactoryManager;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.app.ingestion.util.SftpConstants.*;

/**
 * Created by sgu197 on 9/25/2017.
 */
public class JschPoller implements SourcePollerWithCRC<FileMetaData>{

    private String sftpWorkingDir;
    private static Logger LOG = LoggerFactory.getLogger(JschPoller.class);
    private SftpCommandExecutor sftpCommandExecutor;
    private int md5sumMaxNumFile;
    private static final String MD5_COMMAND ="md5sum";
    private static final int MAX_UNIX_ARG_LENGTH = 30000;// should be less than MAX_ARG_STRENGTH value of linux System
    private String sftpUser;
    private String sftpHost;
    private String sftpKeyLocation;
    private String sftpPort;

    private int numOfTries;
    private int retryTimeGap;
    private JschConnection jschConnection;
    private Map<String,String> prop;

    public JschPoller(){

    }

    public JschPoller(Map<String,String> prop, String sftpWorkingDir, JschConnection jschConnection){

        LOG.info("Creating Directory based poll Strategy...");
        this.prop = prop;
        this.md5sumMaxNumFile = Integer.parseInt(prop.getOrDefault(MD5SUM_MAX_NUM_FILE,"50"));
        this.sftpHost = prop.get(AC_SFTP_SERVER_INPUT_HOST);
        this.sftpUser = prop.get(AC_SFTP_SERVER_INPUT_USER);
        this.sftpKeyLocation = prop.getOrDefault(AC_SFTP_SERVER_INPUT_KEY_LOCATION,null);
        this.sftpPort = prop.getOrDefault(AC_SFTP_SERVER_INPUT_PORT,"22");

        this.jschConnection = jschConnection;

        this.numOfTries = Integer.parseInt(prop.getOrDefault("NUMBER_OF_TRIES","3"));

        retryTimeGap = Integer.parseInt(prop.getOrDefault("RETRY_TIME_GAP","4"));
        this.sftpWorkingDir = sftpWorkingDir;
        this.sftpCommandExecutor = new SftpSimpleCommandExecutor(new JschChannelExec());
        try{
            if(this.jschConnection.isValid() && this.jschConnection!=null)
                jschConnection.getChannelSftp().lstat(sftpWorkingDir);

            }catch (SftpException e){
            LOG.error("Remote Directory -- " + sftpWorkingDir + "does not exist",e);
        }

        LOG.info("Dir Poller ! File Pattern based strategy created!");

    }

    public JschPoller(Map<String,String> prop, String sftpWorkingDir){

        LOG.info("Creating Directory based poll Strategy...");
        this.prop = prop;
        this.md5sumMaxNumFile = Integer.parseInt(prop.getOrDefault(MD5SUM_MAX_NUM_FILE,"50"));
        this.sftpHost = prop.get(AC_SFTP_SERVER_INPUT_HOST);
        this.sftpUser = prop.get(AC_SFTP_SERVER_INPUT_USER);
        this.sftpKeyLocation = prop.getOrDefault(AC_SFTP_SERVER_INPUT_KEY_LOCATION,null);
        this.sftpPort = prop.getOrDefault(AC_SFTP_SERVER_INPUT_PORT,"22");

        this.jschConnection = jschConnection;

        this.numOfTries = Integer.parseInt(prop.getOrDefault("NUMBER_OF_TRIES","3"));

        retryTimeGap = Integer.parseInt(prop.getOrDefault("RETRY_TIME_GAP","4"));
        this.sftpWorkingDir = sftpWorkingDir;
        //this.sftpCommandExecutor = new SftpSimpleCommandExector(new JschChannelExec());


        LOG.info("Dir Poller ! File Pattern based strategy created!");

    }
    @Override
    public List<FileMetaData> poll() throws InterruptedException {

        byte[] checkSumBytes = null;
        int numOfTries = this.numOfTries;
        boolean commandExecSuccessful = true;
        String exceptionMessage = "";
        while(true) {
            LOG.debug("started polling ...");
            List<FileMetaData> fileList = new ArrayList<>();
            if (sftpWorkingDir != null && sftpWorkingDir.length() > 0 && sftpWorkingDir.charAt(sftpWorkingDir.length() - 1) != '/') {
                sftpWorkingDir = sftpWorkingDir + "/";
            }
            try{
                Vector<ChannelSftp.LsEntry> list = this.jschConnection.getChannelSftp().ls(sftpWorkingDir + "*");
                commandExecSuccessful =true;
                for(ChannelSftp.LsEntry entry:list){
                    if(entry.getFilename().equals(".") || entry.getFilename().equals(".."))
                        continue;
                    if(!entry.getAttrs().isDir()){
                        FileMetaData fileMetaData = new FileMetaData(entry.getFilename(),entry.getAttrs().getSize(),
                                entry.getAttrs().getMTime(),"",hasReadPermission(entry.getAttrs().getPermissions()));
                        fileList.add(fileMetaData);
                    }
                }

                LOG.info("Dir -- " +sftpWorkingDir + " -- listed");
                return fileList;
            }catch(SftpException se){
                LOG.error("SFTP Exception raised",se);
                commandExecSuccessful = false;
                if(--numOfTries == 0){
                    System.out.println("noOfTrie"+numOfTries);
                    if(se.getMessage().toLowerCase().contains("refused")){
                        exceptionMessage = "SFTP Server Down";
                    }else if(se.getMessage().toLowerCase().contains("timeout")){
                        exceptionMessage = "Network Failure";
                    } else if(se.getMessage().toLowerCase().contains("4:")){
                        exceptionMessage = "Network Failure";;
                    }else{
                        exceptionMessage = se.getMessage();
                    }
                    ISftpUtil sftpUtil = SftpFactoryManager.getInstance();
                    this.jschConnection = sftpUtil.getNewConnection(prop,sftpHost,Integer.parseInt(sftpPort),
                            sftpUser,sftpKeyLocation);
                    break;
                }
                try{
                    Thread.sleep(this.retryTimeGap);
                }catch (InterruptedException ie){
                    LOG.error("Thread Interrupted Exception",ie);
                    ie.printStackTrace();
                }
                LOG.error("Listing of dir "+sftpWorkingDir + "failed ",se);
            }
            if(commandExecSuccessful == true)
                break;

            }


        return null;
    }

    @Override
    public void stop() throws IOException {

        this.jschConnection.disconnectConnetion();
        LOG.info("Sftp session closed for directory");
    }

    private Map<String,String> md5Checksum(String command) {
        Map<String, String> md5Dictionary = new HashMap<>();
        String commandOutPut = null;
        Object output;
        try {
            output = sftpCommandExecutor.execute(this.jschConnection.getSession(), command);
            commandOutPut = output.toString();
        } catch (Exception ex) {
            LOG.error("Md5sum command execution failed!!", ex);
        }

        String[] arr = commandOutPut.split("\n");
        for (String ele : arr) {
            String[] tempEle = ele.split("\\s+");
            md5Dictionary.put((tempEle[1].replace("\\", "/").substring(tempEle[1].replace("\\", "/").lastIndexOf("/") + 1).trim()), tempEle[0]);

        }
        return md5Dictionary;
    }


    @Override
    public List<FileMetaData> populateMd5Checksum(List<FileMetaData> fileList) {

        StringBuilder command =  new StringBuilder(MD5_COMMAND).append(" ");

        int counter = 0;
        int commandArgLength;
        Map<String,String> md5Dictionary = new HashMap<>();
        int fileListSize = fileList.size();
        if(!fileList.isEmpty()) {
            String fileToProcess = null;
            boolean md5ExecuteFlag = false;
            boolean carryFileFlag = false;
            LOG.info("Start setting Checksum");

            for (FileMetaData fileMetaData : fileList) {
                counter = counter + 1;
                carryFileFlag = false;
                md5ExecuteFlag = false;
                commandArgLength = command.length() + sftpWorkingDir.length() + fileMetaData.getFileName().length();

                if (commandArgLength > MAX_UNIX_ARG_LENGTH) {
                    fileToProcess = fileMetaData.getFileName();
                    md5ExecuteFlag = true;
                    carryFileFlag = true;

                }

                if (!carryFileFlag)
                    //command = command.append(" ").append(sftpWorkingDir).append(fileMetaData.getFileName());
                    command  = command.append(sftpWorkingDir).append(fileMetaData.getFileName());

                if (counter % md5sumMaxNumFile == 0 || fileListSize == counter) {
                    md5ExecuteFlag = true;
                }

                if (md5ExecuteFlag) {
                    md5Dictionary.putAll(md5Checksum(command.toString()));
                    LOG.debug("Total file to fetch checksum - " + fileListSize + " , checksum fetched for " + counter
                            + " files");
                    command.setLength(0);
                    command = command.append(MD5_COMMAND).append(" ");
                    if (carryFileFlag) {
                        command = command.append(" ").append(sftpWorkingDir).append(fileToProcess);
                    }
                }
            }

            if (carryFileFlag) {
                md5Dictionary.putAll(md5Checksum(command.toString()));
            }

            for (FileMetaData ele : fileList) {
                if (md5Dictionary.containsKey(ele.getFileName())) {
                    ele.setMd5Checksum(md5Dictionary.get(ele.getFileName()));
                }
            }

            LOG.info("Checksum set!");
        }
        return fileList;
    }




    @Override
    public void setSftpWorkingDir(String sftpWorkingDir) {

        SftpATTRS attrs = null;
        try{
            ChannelSftp channelSftp = this.jschConnection.getChannelSftp();
            attrs = channelSftp.lstat(sftpWorkingDir);
            System.out.println("work");
        }catch (SftpException e){
            System.out.println("catch block");
                    LOG.error("Remote Directort -- "+sftpWorkingDir + " does not exist");
        }
        this.sftpWorkingDir  = sftpWorkingDir.replace("\\","/");
    }


    public boolean hasReadPermission(int permission){
        if((permission & 4) != 0 || (permission & 32) != 0)
                return true;
            else
                return false;

    }
}

