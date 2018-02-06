package com.app.ingestion.util;

import com.sun.javafx.image.impl.IntArgb;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class ExponentialBackOff {

    private Instant endTime = null;

    private Duration step;
    private Duration cap;

    private int iteration = 0;
    private static int MAX_ITERATION = 20;

    public ExponentialBackOff(Duration step, Duration cap) {
       this(step,cap,0);
    }

    public ExponentialBackOff(Duration step, Duration cap,int iteration) {
        this.step = step;
        this.cap = cap;
        this.iteration = iteration;
        this.endTime = Instant.now().plusMillis(interval(step,cap,iteration));
    }

    public long interval(Duration step, Duration cap,int iteration){
        long stepLong= step.toMillis()*(long)Math.pow(2,iteration);
        return Math.min(cap.toMillis(),stepLong);
    }

    public Duration remaining(){
        return Duration.between(Instant.now(),endTime);
    }


    public boolean passed(){
        return Instant.now().isAfter(this.endTime);
    }

    public ExponentialBackOff nextRun(){
        if(this.iteration < MAX_ITERATION){
            this.iteration = this.iteration + 1;
        }
        return  new ExponentialBackOff(this.step,this.cap,this.iteration);
    }

    public boolean equalsTo(ExponentialBackOff eb){
        return (eb.step.getSeconds() == this.step.getSeconds()
                &&
                eb.cap.getSeconds() == this.cap.getSeconds());
    }

    public ExponentialBackOff resetBackOff(int backOffStep,int backOffCap) {
        if( this.iteration == 0)
            return this;

            return new ExponentialBackOff(Duration.ZERO.ofSeconds(backOffStep),Duration.ofSeconds(backOffCap));
        }

     public void resetIteration(){
        this.iteration = 0;

    }

}
