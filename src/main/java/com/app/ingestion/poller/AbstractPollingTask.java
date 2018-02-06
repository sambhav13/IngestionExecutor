package com.app.ingestion.poller;

import com.app.ingestion.model.FileMetaData;
import com.app.ingestion.model.Record;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sgu197 on 9/25/2017.
 */
public abstract class AbstractPollingTask {

    abstract protected  List<Record> transferValidFiles(List<FileMetaData> fileList) throws IOException,InterruptedException;

    abstract protected List<FileMetaData> filterOldFiles(List<FileMetaData> fileList);

    abstract protected void handleInvalidFiles(Map<FileMetaData,Boolean> validationResultMap);

    abstract protected List<Record> createSourceRecords(List<FileMetaData> metaDataList);

    protected List<FileMetaData> filterValidationResults(Map<FileMetaData,Boolean> validationResultMap,
                                                         boolean validationflag){
        List<FileMetaData> results =  new ArrayList<>();
        for(FileMetaData fileMetaData:validationResultMap.keySet()){
            if(validationResultMap.get(fileMetaData) == validationflag){
                    results.add(fileMetaData);
            }
        }
        return results;
    }

    public List<FileMetaData> filterReadPermission(List<FileMetaData> metaDataList,Boolean readPermissionFlag){
        if(metaDataList != null && metaDataList.size()>0){
            List<FileMetaData> result = new ArrayList<>();
            for(FileMetaData itr:metaDataList){
                if(itr.getReadPermissionFlag().compareTo(readPermissionFlag)==0)
                    result.add(itr);
            }
            return result;
        }else
            return metaDataList;
    }


    public void notifyInvalidFiles(List<FileMetaData> metaDataList,String notificationType){

    }
    protected void notifyFileTransfer(final String fileName,final String src,final String dst,
                                      long size,
                                      final boolean success,
                                      final String errorMessage,
                                      final String notificationType,
                                      String successTopic,
                                      String errorTopic){

    }

}
