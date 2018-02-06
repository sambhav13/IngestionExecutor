package com.app.ingestion.poller;

import java.io.IOException;
import java.util.List;

/**
 * Created by sgu197 on 9/25/2017.
 */
public interface SourcePoller<T> {


    public List<T> poll() throws InterruptedException;

    public void stop() throws IOException;
}
