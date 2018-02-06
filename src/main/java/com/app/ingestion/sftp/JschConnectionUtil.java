package com.app.ingestion.sftp;

import com.app.ingestion.model.JschConnection;
import com.app.ingestion.util.ExponentialBackOff;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static com.app.ingestion.util.SftpConstants.*;
import static com.app.ingestion.util.SourceConstants.RETRY_ATTEMPT;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class JschConnectionUtil implements  IJschConnectionUtil{

    private static Logger LOG = LoggerFactory.getLogger(JschConnectionUtil.class);
    private IJschDependency jschDependency;

    static int retryTimeGap;
    static int backOffStep;
    static int backOffCap;

    static int numberOfSessions;
    static int numberOfChannels;


    @Override
    public int getSessionCount() {
        return JschConnectionUtil.numberOfSessions;
    }

    @Override
    public int getChannelCount() {
        return JschConnectionUtil.numberOfChannels;
    }

    public JschConnectionUtil(IJschDependency jschDependency) {
        this.jschDependency = jschDependency;
    }

    @Override
    public JschConnection getNewConnection(Map<String, String> props, String sftpHost, int sftpPort, String sftpUser, String sftpKeyLocation) {

        JschConnection connection;
        backOffStep = Integer.parseInt(props.getOrDefault(BACK_OFF_STEP,"15"));
        backOffCap = Integer.parseInt(props.getOrDefault(BACK_OFF_CAP,"30"));

        ExponentialBackOff localBackOff = new ExponentialBackOff(Duration.ofSeconds(backOffStep),
                                                                Duration.ofSeconds(backOffCap));

        while(true){
            try{
                connection = tryToEstablishConnection(props,sftpHost,sftpPort,sftpUser,sftpKeyLocation);
                LOG.info("sftp connection established");
                return connection;
            }catch(IOException ioE){
                localBackOff =  localBackOff.nextRun();
                LOG.info("failed to get connection - waiting for local backoff , time remaining "+localBackOff.remaining());
                while(!localBackOff.passed()){
                    delay(backOffStep);
                }
            }
        }

    }

    @Override
    public JschConnection tryToEstablishConnection(Map<String, String> props, String sftpHost, int sftpPort,
                                                   String sftpUser, String sftpKeyLocation) throws IOException{
        JschConnection connection;
        ChannelSftp channelSftp;
        Session session;

        backOffStep = Integer.parseInt(props.getOrDefault(BACK_OFF_STEP,"15"));
        backOffCap = Integer.parseInt(props.getOrDefault(BACK_OFF_CAP,"30"));

        int attempts = Integer.parseInt(props.getOrDefault(RETRY_ATTEMPT,"3"));

        LOG.info("Trying to establish Connection, attempts: "+attempts);

        while(attempts > 0){
            session = this.jschDependency.createNewSftpSession(props,sftpHost,sftpPort,sftpUser,sftpKeyLocation);
            if(session!=null){
                JschConnectionUtil.numberOfSessions++;
                channelSftp = this.jschDependency.createdNewSftpChannel(session,sftpHost,sftpPort,sftpUser,sftpKeyLocation);
                if(channelSftp!=null){
                    JschConnectionUtil.numberOfChannels++;
                    connection = new JschConnection(session,channelSftp);
                    if(connection.isValid()){
                        return connection;
                    }
                }
            }
            LOG.info("Current Number of Sessions, channels are " +JschConnectionUtil.numberOfSessions + "and "
                +JschConnectionUtil.numberOfChannels);
            attempts--;
            LOG.info("Connection is not valid - ATTEMPTS LEFT: "+attempts);
            //waiting for 10 sec - cant use Thread.sleep in synchronized method
            delay(10);
        }
        throw new IOException("Could not establish sftp connection");

    }

    public synchronized void delay(int secs){
        long now;
        long start =  System.currentTimeMillis();
        do{
            now = System.currentTimeMillis();

        }while((now - start)< secs*1000);
    }
}
