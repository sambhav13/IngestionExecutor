package com.app.ingestion.poller;

import com.app.ingestion.model.DirectoryMetaData;
import com.app.ingestion.model.JschConnection;
import com.app.ingestion.model.TaskMetaData;
import com.app.ingestion.poller.task.PollingRunnable;
import com.app.ingestion.poller.task.PollingTask;
import com.app.ingestion.sftp.ISftpUtil;
import com.app.ingestion.sftp.SftpFactoryManager;
import com.app.ingestion.util.ConfigUtil;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.app.ingestion.util.SftpConstants.*;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class PollingService {

    private String dirId;
    private String dirPath;
    private String sourceName;
    private String assetClass;
    private boolean isMaster;
    private String pattern;

    private String sftpUser;
    private String sftpHost;
    private String sftpKeyLocation;
    private String sftpPort;


    private JschConnection[] jschConnectionArr;

    private Map<String,String> props;
    private TaskMetaData taskMetaData;

    private static Logger LOG  = LoggerFactory.getLogger(PollingService.class);

    public static void main(String[] args){

        //start the process
        PollingService pollingService = new PollingService();
        pollingService.readProperties();
        pollingService.startPolling();
    }



    private void readProperties() {

        Properties properties = new Properties();
        this.props = new HashMap<String,String>();
        try {
            //properties.load(this.getClass().getClassLoader().getResourceAsStream("connect-sftp-source.properties"));

            properties.load(this.getClass().getClassLoader().getResourceAsStream( System.getProperty("config.file")));
            // properties.load(this.getClass().getClassLoader().getResourceAsStream("connect-sftp-source-test.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String key:properties.stringPropertyNames()){
            String value = properties.getProperty(key);
            this.props.put(key,value);
        }
    }

    private void startPolling() {

        List<DirectoryMetaData> dirList = ConfigUtil.getConfiguredDirs(props);

        String sftpHosts = props.get(AC_SFTP_SERVER_INPUT_HOST);
        String[] hosts = sftpHosts.split(VALUE_SEPARATOR);
        LOG.info("Number of sftp hosts" +sftpHosts.length());

        List<Map<String,String>> configs = new ArrayList<>();
        for(String host:hosts){
            for(DirectoryMetaData directoryMetaData:dirList){
                boolean isMaster = true;
                for(String pattern:directoryMetaData.getAllowedPatterns()){
                    Map<String,String> taskProperties = new HashMap();
                    taskProperties.putAll(props);
                    taskProperties.put(AC_TASK_DIR_ID,directoryMetaData.getId());
                    taskProperties.put(MASTER_TASK,String.valueOf(isMaster));
                    taskProperties.put(TASK_PATTERN,pattern);
                    taskProperties.put(TASK_HOST,host);
                    configs.add(taskProperties);
                    isMaster = false;


                }
            }
        }

        //initializing the Executor Service
        ExecutorService executorService = Executors.newFixedThreadPool(configs.size());
        List<Future<String>> results = new ArrayList<>();

        this.jschConnectionArr =  new JschConnection[configs.size()];
        //Decide Number of Parallel threads
        for(int i=0;i<configs.size();i++){
            Map<String,String> props = configs.get(i);
            dirId = props.get(AC_TASK_DIR_ID);
            DirectoryMetaData dirMetaData = ConfigUtil.getConfiguredDirForTask(dirId,props);
            this.sftpHost = props.get(TASK_HOST);
            this.isMaster = Boolean.parseBoolean(props.getOrDefault(MASTER_TASK, "false"));
            this.pattern = props.get(TASK_PATTERN);
            this.sftpPort = props.getOrDefault(AC_SFTP_SERVER_INPUT_PORT,"22");
            this.sftpUser = props.getOrDefault(AC_SFTP_SERVER_INPUT_USER,"cloudera");
            this.sftpKeyLocation = props.getOrDefault(AC_SFTP_SERVER_INPUT_KEY_LOCATION,"");
            props.put(AC_SFTP_SERVER_INPUT_HOST,sftpHost);


            ISftpUtil  sftpUtil = SftpFactoryManager.getInstance();
            try{
                this.jschConnectionArr[i] =  sftpUtil.getNewConnection(props,sftpHost,Integer.parseInt(sftpPort),
                        sftpUser,sftpKeyLocation);
                ChannelSftp channelSftp =  this.jschConnectionArr[i].getChannelSftp();
                Vector<ChannelSftp.LsEntry> entries = channelSftp.ls("/home/cloudera/executor/sftp/FI/");
                checkEntries(entries);
            }catch (Exception ex){
                ex.printStackTrace();
            }

            Future<String> result = executorService.submit(new PollingTask(this.jschConnectionArr[i],props));
            results.add(result);

        }


            /*for (int i = 0; i < configs.size(); i++) {
            if(jschConnectionArr[i]!=null)
                this.jschConnectionArr[i].disconnectConnetion();
            }*/


        for(Future<String> re:results){
            try{
                System.out.println("result -->"+re.get());

            }catch(InterruptedException ie){
                ie.printStackTrace();

            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();

    }


    public void checkEntries(Vector<ChannelSftp.LsEntry> entries){

        for(ChannelSftp.LsEntry entry:entries){

            SftpATTRS entryAttrs = entry.getAttrs();
            if(!entryAttrs.isDir()){
                System.out.println("Filename is "+entry.getFilename() +
                        " with size: "+(entryAttrs.getSize()/(1024*1024)));
            }
        }
    }


}
