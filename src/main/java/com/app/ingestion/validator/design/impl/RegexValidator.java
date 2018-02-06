package com.app.ingestion.validator.design.impl;

import com.app.ingestion.model.FileMetaData;
import com.app.ingestion.validator.design.Criterion;
import com.app.ingestion.validator.design.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by sgu197 on 9/24/2017.
 */
public class RegexValidator implements Validator<FileMetaData> {

    private static Logger LOG = LoggerFactory.getLogger(RegexValidator.class);

    private Criterion crt;

    public RegexValidator(Criterion crt){
        this.crt = crt;
        LOG.debug("RegexValidator is initialized with RegexCriterion object");

    }


    @Override
    public Boolean isValid(FileMetaData meta){
        LOG.debug("RegexValidator received FileMetaData object  performing validation");
        if( (meta.getFileName().matches((String)crt.producePositiveCriteria()) )
                &&
                (!meta.getFileName().matches((String)crt.produceNegativeCriteria()) ) ){

            LOG.debug("filename is valid, returning TRUE");
            return true;
        }
        LOG.debug("filename is not valid , returning FALSE");
        return false;

    }



    @Override
    public Map<FileMetaData, Boolean> isValid(List<FileMetaData> metaList) {
        Map<FileMetaData,Boolean> map =  new HashMap<>();
        if(metaList !=null){
            LOG.debug("RegexValidator received list of FileMetaData objects, performing validation");
            for(FileMetaData meta:metaList){
                LOG.debug("validating file: "+ meta.getFileName());
                map.put(meta,isValid(meta));
            }
            LOG.debug("returning Map of <FileMetaData,Boolean> tuples");
        }
        return map;
    }


}
