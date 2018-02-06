package com.app.ingestion.poller;

import com.app.ingestion.poller.jsch.JschChannelExec;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by sgu197 on 10/4/2017.
 */
public class SftpSimpleCommandExecutor implements SftpCommandExecutor<String> {

    private JschChannelExec jschChannelExec;

    private static Logger log = LoggerFactory.getLogger(SftpSimpleCommandExecutor.class);


    public SftpSimpleCommandExecutor(){

    }

    public SftpSimpleCommandExecutor(JschChannelExec jschChannelExec){
        this.jschChannelExec = jschChannelExec;
    }

    @Override
    public String execute(Session session, String command) {

        String output = new String("");
        ChannelExec channelExec;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            byte[] tmp = null;
            InputStream in = null;
            String x = null;
            int i = 0;

            this.jschChannelExec.setChannelExec(channelExec);
            this.jschChannelExec.setErrStream(System.err);
            in = this.jschChannelExec.getInputStream();
            this.jschChannelExec.setCommand(command);

            log.debug("Connecting server to execute command");
            this.jschChannelExec.connect();
            log.debug("connection established");
            tmp = new byte[5];
            log.debug("fetching command output");
            output = this.jschChannelExec.getCommandOutput(in, tmp, i, output, x);
            log.debug("Command output fetched");
            this.jschChannelExec.disConnect();
            log.debug("Execution channel closed");
            return output.toString();
        }catch (Exception e){
            log.error("channelExec failed --",e);
        }
        return null;
    }
}
