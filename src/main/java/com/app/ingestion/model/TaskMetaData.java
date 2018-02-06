package com.app.ingestion.model;

import java.util.List;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class TaskMetaData {

    String sourceName;
    String dir;
    String assetClass;
    boolean isMaster;
    List<String> allowedPatterns;
    List<String> notAllowedPatterns;
    List<String> ignoredPatterns;
    boolean useCheckSum;

    public TaskMetaData(String sourceName, String dir, String assetClass, boolean isMaster, List<String> allowedPatterns, List<String> notAllowedPatterns, List<String> ignoredPatterns, boolean useCheckSum) {
        this.sourceName = sourceName;
        this.dir = dir;
        this.assetClass = assetClass;
        this.isMaster = isMaster;
        this.allowedPatterns = allowedPatterns;
        this.notAllowedPatterns = notAllowedPatterns;
        this.ignoredPatterns = ignoredPatterns;
        this.useCheckSum = useCheckSum;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public List<String> getAllowedPatterns() {
        return allowedPatterns;
    }

    public void setAllowedPatterns(List<String> allowedPatterns) {
        this.allowedPatterns = allowedPatterns;
    }

    public List<String> getNotAllowedPatterns() {
        return notAllowedPatterns;
    }

    public void setNotAllowedPatterns(List<String> notAllowedPatterns) {
        this.notAllowedPatterns = notAllowedPatterns;
    }

    public List<String> getIgnoredPatterns() {
        return ignoredPatterns;
    }

    public void setIgnoredPatterns(List<String> ignoredPatterns) {
        this.ignoredPatterns = ignoredPatterns;
    }

    public boolean isUseCheckSum() {
        return useCheckSum;
    }

    public void setUseCheckSum(boolean useCheckSum) {
        this.useCheckSum = useCheckSum;
    }
}
