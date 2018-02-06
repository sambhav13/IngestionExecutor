package com.app.ingestion.util;

/**
 * Created by sgu197 on 9/26/2017.
 */
public class StopWatch {
    private long startTime;

    public StopWatch(){
        startTime = System.currentTimeMillis();
    }

    public StopWatch(final String msg){
        startTime = System.currentTimeMillis();
    }

    public final double getElapsedTime(){
        long endTime = System.currentTimeMillis();
        return (double) (endTime - startTime) /(1000);
    }
}
