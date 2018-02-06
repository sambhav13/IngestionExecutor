package com.app.ingestion.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

import java.util.Map;

/**
 * Created by sgu197 on 9/24/2017.
 */
public interface IJschDependency {

    Session createNewSftpSession(Map<String,String> props, String sftpHost, int sftpPort,
                                 String sftpUser, String sftpKeyLocation);
    ChannelSftp createdNewSftpChannel(Session session,String sftpHost,int sftpPort,
                                      String sftpUser,String sftpKeyLocation);

   // boolean issueKerberosToken(Map<String,String> props);

}
