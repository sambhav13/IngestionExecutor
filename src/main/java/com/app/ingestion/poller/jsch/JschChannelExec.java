package com.app.ingestion.poller.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by sgu197 on 10/4/2017.
 */
public class JschChannelExec {

    private ChannelExec channelExec;

    public JschChannelExec(){

    }

    public void setChannelExec(ChannelExec channelExec){
        this.channelExec = channelExec;
    }

    public InputStream getInputStream() throws IOException{

        return this.channelExec.getInputStream();
    }

    public void setErrStream(OutputStream out) {
        this.channelExec.setErrStream(out);
    }


    public void setCommand(String command){
        this.channelExec.setCommand(command);
    }

    public void connect() throws JSchException{
        this.channelExec.connect();
    }

    public void disConnect(){
        this.channelExec.disconnect();
    }


    public String getCommandOutput(InputStream in,
                                   byte[] tmp,
                                   int i,
                                   String output,
                                   String x) throws IOException{

        while(true){
            while(in.available() > 0 ){
                i = in.read(tmp,0,5);
                if( i < 0 )
                    break;
                x = new String(tmp,0,i);
                output = output + x;
            }
            if(this.channelExec.isClosed()){
                if(in.available() > 0 ) continue;
                break;
            }
        }
        return output;
    }
}
