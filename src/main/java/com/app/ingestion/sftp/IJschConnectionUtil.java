package com.app.ingestion.sftp;

import com.app.ingestion.model.JschConnection;

import java.io.IOException;
import java.util.Map;

/**
 * Created by sgu197 on 9/24/2017.
 */
interface IJschConnectionUtil {

    int getSessionCount();
    int getChannelCount();

    JschConnection getNewConnection(Map<String,String> props, String sftpHost,
                                    int sftpPort, String sftpUser, String sftpKeyLocation) ;

    JschConnection tryToEstablishConnection(Map<String,String> props, String sftpHost,
                                            int sftpPort, String sftpUser, String sftpKeyLocation) throws IOException;
}
