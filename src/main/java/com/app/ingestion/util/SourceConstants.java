package com.app.ingestion.util;

import scala.Int;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class SourceConstants {

    public static final String RETRY_ATTEMPT = "retry.attempt";

    public static String getBatchType(final String batchNumber){

        int batchValue = Integer.parseInt(batchNumber);
        String ret;
        if(batchValue == 1){
            ret = "INITIAL";
        }else{
            ret = "INCREMENTAL";
        }
        return ret;
    }
}
