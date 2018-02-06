package com.app.ingestion.util;

import com.app.ingestion.model.DirectoryMetaData;
import com.app.ingestion.model.TaskMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.app.ingestion.util.SftpConstants.*;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class ConfigUtil {

    private static Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

    public static List<DirectoryMetaData> getConfiguredDirs(Map<String,String> props) {

        LOG.info("Reading directory abd patterns");
        List<DirectoryMetaData> dirList =  new ArrayList<>();
        for(Map.Entry<String,String> property:props.entrySet()){

            if(property.getKey().startsWith(AC_DIR_INPUT) && !(property.getKey().contains(AC_DIR_ALLOWED_PATTERN)
                    || property.getKey().contains(AC_DIR_NOT_ALLOWED_PATTERN)
                    || property.getKey().contains(AC_DIR_FILE_IN_TRANSFER_PATTERN)
                    || property.getKey().contains(AC_DIR_SOURCE_NAME)
                    ||property.getKey().contains(AC_DIR_ASSET_CLASS))){

                String dirPath = property.getValue();
                String dirId = property.getKey().replace(AC_DIR_INPUT + "." ,"");
                String sourceName = props.get(property.getKey() + AC_DIR_SOURCE_NAME);

                String assetClass = props.get(AC_DIR_INPUT+ "." + dirId + AC_DIR_ASSET_CLASS);

                List<String> allowedPatterns = getAllPropsStartsWith(AC_DIR_INPUT + "." + dirId + AC_DIR_ALLOWED_PATTERN,props);
                List<String> notAllowedPatterns = getAllPropsStartsWith(AC_DIR_INPUT + "." + dirId + AC_DIR_NOT_ALLOWED_PATTERN,props);
                List<String> inTransferredPatterns = getAllPropsStartsWith(AC_DIR_INPUT + "." + dirId + AC_DIR_FILE_IN_TRANSFER_PATTERN,props);
                DirectoryMetaData dir = new DirectoryMetaData(dirId,dirPath,sourceName,allowedPatterns,notAllowedPatterns,
                        inTransferredPatterns,assetClass);
                dirList.add(dir);

            }
        }
        LOG.info("Number of directories configured" + dirList.size());
        return dirList;

    }

    public static List<String> getAllPropsStartsWith(String key,Map<String,String> props) {
        LOG.info("Finding properties start with " + key);

        List<String> listValues = new ArrayList<>();
        for (Map.Entry<String, String> patterns : props.entrySet()) {
            if (patterns.getKey().startsWith(key)) {
                listValues.add(patterns.getValue());
            }
        }
        LOG.info("Number of properties starts wuth "+ key+ " = " + listValues.size());
        return  listValues;
    }

    public static DirectoryMetaData getConfiguredDirForTask(String dirId, Map<String,String> props){
        LOG.info("Finding directory and patterns for directory"+ dirId);
        DirectoryMetaData dirMetaData =  null;

        for(Map.Entry<String,String> property:props.entrySet()) {
            if (property.getKey().startsWith(AC_DIR_INPUT + "." + dirId) &&
                    !(property.getKey().contains(AC_DIR_ALLOWED_PATTERN)
                            || property.getKey().contains(AC_DIR_NOT_ALLOWED_PATTERN)
                            || property.getKey().contains(AC_DIR_FILE_IN_TRANSFER_PATTERN)
                            || property.getKey().contains(AC_DIR_SOURCE_NAME)
                            || property.getKey().contains(AC_DIR_ASSET_CLASS))) {

                String dirPath =  property.getValue();
                String sourceName = props.get(property.getKey() + AC_DIR_SOURCE_NAME);
                String assetClass = props.get(property.getKey() + AC_DIR_ASSET_CLASS);
                List<String> allowedPatterns = getAllPropsStartsWith(AC_DIR_INPUT + "." + dirId + AC_DIR_ALLOWED_PATTERN
                    ,props);
                List<String> notAllowedPatterns = getAllPropsStartsWith(AC_DIR_INPUT + "." + dirId + AC_DIR_NOT_ALLOWED_PATTERN
                        ,props);
                List<String> inTransferPatterns = getAllPropsStartsWith(AC_DIR_INPUT + "." + dirId + AC_DIR_FILE_IN_TRANSFER_PATTERN
                        ,props);

                dirMetaData =  new DirectoryMetaData(dirId,dirPath,sourceName,allowedPatterns,notAllowedPatterns,inTransferPatterns,assetClass);
                break;
            }
        }

        LOG.info("Directory found with path " + dirMetaData.getPath());
        return dirMetaData;
    }

public static TaskMetaData prepareTaskMetaData(DirectoryMetaData dirMetaData,Map<String,String> props,boolean isMaster,
                                               String pattern,boolean checkSumCheck){

        List<String> ignorePatterns = new ArrayList<>();
        if(isMaster) {
            ignorePatterns.addAll(dirMetaData.getAllowedPatterns());
            ignorePatterns.addAll(dirMetaData.getInTransferPatterns());
            ignorePatterns.remove(pattern);
        }else{
            ignorePatterns  = dirMetaData.getInTransferPatterns();
        }
        return new TaskMetaData(dirMetaData.getSourceName(),dirMetaData.getPath(),dirMetaData.getAssetClass(),isMaster,
                Arrays.asList(pattern),dirMetaData.getNotAllowedPatterns(),ignorePatterns,checkSumCheck);
}

}
