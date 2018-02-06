package com.app.ingestion.poller.task;

/**
 * Created by sgu197 on 9/25/2017.
 */
public class PollingRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("result-->"+Thread.currentThread().getName());
    }
}
