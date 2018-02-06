package com.app.ingestion.sftp;

import com.jcraft.jsch.JSch;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class SftpFactoryManager {

    synchronized  public static ISftpUtil getInstance() {
        IJschDependency jschDependency = new JschDependency(
                new JSch()
        );
        IJschConnectionUtil jschConnectionUtil = new JschConnectionUtil(jschDependency);
        ISftpUtil factory = new SftpUtil(jschConnectionUtil);
        return factory;
    }
}
