package com.app.ingestion.sftp;

import com.app.ingestion.model.JschConnection;

import java.io.IOException;
import java.util.Map;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class SftpUtil implements ISftpUtil {

    private IJschConnectionUtil jschConnectionUtil;

    public SftpUtil(IJschConnectionUtil jschConnectionUtil) {
        this.jschConnectionUtil = jschConnectionUtil;
    }

    @Override
    public JschConnection getNewConnection(Map<String, String> props, String sftpHost, int sftpPort, String sftpUser, String sftpKeyLocation) {
        return this.jschConnectionUtil.getNewConnection(props,sftpHost,sftpPort,sftpUser,sftpKeyLocation);
    }

    @Override
    public JschConnection tryToEstablishConnection(Map<String, String> props, String sftpHost, int sftpPort, String sftpUser, String sftpKeyLocation) throws IOException {
        return this.jschConnectionUtil.tryToEstablishConnection(props,sftpHost,sftpPort,sftpUser,sftpKeyLocation);
    }
}
