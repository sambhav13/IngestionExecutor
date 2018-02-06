package com.app.ingestion.fileselector.impl;

import com.app.ingestion.fileselector.FileSelector;
import com.app.ingestion.model.FileMetaData;
import com.app.ingestion.validator.design.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgu197 on 9/25/2017.
 */
public class FileNamePatternSelector implements FileSelector {

    private Validator validator;
    private static Logger LOG = LoggerFactory.getLogger(FileNamePatternSelector.class);

    public FileNamePatternSelector(Validator validator) {
        LOG.info("Creating Directory based Poll Strategy");
        this.validator = validator;
    }

    @Override
    public List<FileMetaData> filter(List<FileMetaData> list) throws InterruptedException {

        List<FileMetaData> fileList = new ArrayList<>();
        if(list != null){
            for(FileMetaData metaData : list){
                if(validator.isValid(metaData))
                    fileList.add(metaData);
            }
        }
        return fileList;
    }
}
