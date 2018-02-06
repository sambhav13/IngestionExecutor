package com.app.ingestion.poller;

import com.jcraft.jsch.Session;

/**
 * Created by sgu197 on 10/4/2017.
 */
public interface SftpCommandExecutor<T> {

    T execute(Session session, String command);
}
