package com.app.ingestion.model;

import java.util.List;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class DirectoryMetaData {

    private String id;
    private String path;
    private String sourceName;
    private List<String> allowedPatterns;
    private List<String> notAllowedPatterns;
    private List<String> inTransferPatterns;
    private String assetClass;

    public DirectoryMetaData(String id, String path, String sourceName, List<String> allowedPatterns, List<String> notAllowedPatterns, List<String> inTransferPatterns, String assetClass) {
        this.id = id;
        this.path = path;
        this.sourceName = sourceName;
        this.allowedPatterns = allowedPatterns;
        this.notAllowedPatterns = notAllowedPatterns;
        this.inTransferPatterns = inTransferPatterns;
        this.assetClass = assetClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
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

    public List<String> getInTransferPatterns() {
        return inTransferPatterns;
    }

    public void setInTransferPatterns(List<String> inTransferPatterns) {
        this.inTransferPatterns = inTransferPatterns;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }
}
