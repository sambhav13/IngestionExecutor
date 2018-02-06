package com.app.ingestion.model;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class JschConnection {

    private Session session;

    private ChannelSftp channelSftp;

    private ChannelSftp newChannel;

    public JschConnection(){

    }

    public JschConnection(Session session, ChannelSftp channelSftp) {
        this.session = session;
        this.channelSftp = channelSftp;
    }

    public Session getSession() {
        return session;
    }

    public ChannelSftp getChannelSftp() {
        return channelSftp;
    }

    public ChannelSftp getNewChannel(){
        try {
            newChannel =  ((ChannelSftp)
                    this.session.openChannel("sftp"));
            newChannel.connect();
            return newChannel;
        }catch (JSchException jsce){
            jsce.printStackTrace();
        }
        return null;
    }

    public void closeNewChannel(){
        this.newChannel.disconnect();
    }

    public final boolean isValid(){
        return (this.session != null
                &&
                this.channelSftp != null
                &&
                this.session.isConnected()
                &&
                this.channelSftp.isConnected());
    }

    public final void disconnectConnetion(){
        if(this.session !=null && this.channelSftp != null){
            this.session.disconnect();
            this.channelSftp.disconnect();
        }
    }
}
