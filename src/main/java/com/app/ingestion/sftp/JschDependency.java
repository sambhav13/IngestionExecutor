package com.app.ingestion.sftp;

import com.app.ingestion.model.JschConnection;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static com.app.ingestion.util.SftpConstants.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class JschDependency implements IJschDependency{

    private JSch instance = new JSch();
    //private IKerberosUtil kerberosUtil;
    private static Logger LOG = LoggerFactory.getLogger(JschDependency.class);

    public JschDependency(){

    }

    //public JschDependency(JSch instance,IkerberosUtil kerberosUtil) {
    public JschDependency(JSch instance) {
        this.instance = instance;
    }

    public JSch getInstance(){
        return instance;
    }

    public Session getSession(String username,String host,int port){

        Session session = null;
        try {
            session = instance.getSession(username,host,port);
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return session;
    }

    public void addIdentity(String prvKey){
        try {
            instance.addIdentity(prvKey);
        } catch (JSchException e) {
            LOG.info("failed to add private key to jsch identity");
            e.printStackTrace();
        }
    }
    @Override
    public Session createNewSftpSession(Map<String, String> props, String sftpHost, int sftpPort, String sftpUser, String sftpKeyLocation) {

        LOG.info("Creating Sftp based connection....");
        Session session = null;

        String authMethod = props.getOrDefault(AUTHENTICATION_METHOD,"kerberos");
        LOG.debug("auth method is: "+authMethod);

        if(!(authMethod.equals("keyBased") || authMethod.equals("kerberos"))) {
            LOG.error("Authentication Method -- " + authMethod + " Not allowed. Allowed Authentication Methods are -- kerberos/KeyBased");
            return session;
        }
        if(authMethod.equals("kerberos"))
            sftpUser = props.get(KERBEROS_PRINCIPAL);

        if(sftpKeyLocation != null && sftpKeyLocation.length() > 0  && authMethod.equals("keyBased"))
            addIdentity(sftpKeyLocation);

        //creating session
        session = getSession(sftpUser,sftpHost,sftpPort);

        //setting props
        Properties config = new Properties();
        config.put("StrictHostKeyChecking","no");
        config.put("PreferredAuthentication","gssapi-with-mic,publickey");
        session.setConfig(config);
        int sessionTimeout = Integer.parseInt(props.getOrDefault(SESSION_TIMEOUT,"600000"));

        //try to connect
        try{
            session.connect(sessionTimeout);
            LOG.info("sftp session is connected!");
        }catch(JSchException ex){
            ex.printStackTrace();
            LOG.debug(ex.getMessage(),ex);
            if(ex.getMessage().contains("Auth fail") && authMethod.equals("kerberos")) {
                /*if(issueKerberosToken(props)){
                        try {
                            session.connect();
                            LOG.info("sftp session is connected");
                        }catch (JSchException jschE){
                            LOG.info("COULD NOT CONNECT THE SESSION TO SFTP");
                            ex.printStackTrace();
                        }
                    }*/
                }
        }
        return session;
    }

    @Override
    public ChannelSftp createdNewSftpChannel(Session session, String sftpHost, int sftpPort, String sftpUser, String sftpKeyLocation) {
        ChannelSftp sftpChannel = null;
        if(session !=null && session.isConnected()){
            try{
                sftpChannel = (ChannelSftp)session.openChannel("sftp");
                LOG.info("fresh channel created");

                if(!sftpChannel.isConnected())
                    try{
                        sftpChannel.connect();
                        LOG.debug("Channel connected");

                    }catch (JSchException je){
                        je.printStackTrace();
                        LOG.error("JSCH EXception raised while connecting the sftp channel",je);
                    }

                }catch(JSchException je){
                LOG.error("JSCH Exception raised while creating sftp channel",je);
            }
        }else{
            LOG.error("SFTP SESSION IS NOT AVAILABLE");
        }

        return sftpChannel;
    }

    /*@Override
    public boolean issueKerberosToken(Map<String, String> props) {

        LOG.info("trying to re-issue token");
        String kerberosPrincipal = props.get(KERBEROS_PRINCIPAL);
        String keyTabFile = props.get(KEYTAB_FILE);
        if(kerberosPrincipal != null && kerberosPrincipal.length() > 0 && keyTabFile !=null && keyTabFile.length()>0) {
            String[] kinitArg = {"-k", "-t", keyTabFile, kerberosPrincipal};
            if (this.kerberosUtil.issueToken(kinitArg)) {
                LOG.debug("kerberos token re-issue");
                return TRUE;
            } else {
                LOG.info("Either Kerberos Principal or KeyTab File not passed properly");
                return FALSE;

            }
        }
    }*/
}
