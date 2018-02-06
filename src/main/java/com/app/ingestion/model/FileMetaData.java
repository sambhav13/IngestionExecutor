package com.app.ingestion.model;

import java.io.File;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class FileMetaData {

    private long size;
    private long offset;
    private long mtime;
    private String md5Checksum;
    private String fileName;
    private Boolean readPermissionFlag;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getMtime() {
        return mtime;
    }

    public void setMtime(long mtime) {
        this.mtime = mtime;
    }

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getReadPermissionFlag() {
        return readPermissionFlag;
    }

    public void setReadPermissionFlag(Boolean readPermissionFlag) {
        this.readPermissionFlag = readPermissionFlag;
    }

    public FileMetaData(String fileName){
        this.fileName = fileName;
    }

    public FileMetaData(String fileName, long size, long mtime,String md5Checksum,Boolean readPermissionFlag){
        this.size = size;
        this.mtime = mtime;
        this.md5Checksum = md5Checksum;
        this.fileName = fileName;
        this.readPermissionFlag = readPermissionFlag;
    }

    public FileMetaData(long size, long offset, long mtime, String md5Checksum, String fileName, Boolean readPermissionFlag) {
        this.size = size;
        this.offset = offset;
        this.mtime = mtime;
        this.md5Checksum = md5Checksum;
        this.fileName = fileName;
        this.readPermissionFlag = readPermissionFlag;
    }

    public FileMetaData(long size,long offset,long mtime,String md5Checksum){

        this.size = size;
        this.offset = offset;
        this.mtime = mtime;
        this.md5Checksum = md5Checksum;
    }

    public FileMetaData(long size,long mtime,String md5Checksum){

        this.size = size;
        this.mtime = mtime;
        this.md5Checksum = md5Checksum;
    }

    public int hashCode(){
        return  fileName.hashCode();
    }

    public boolean equals(Object obj){
        if(obj instanceof FileMetaData){
            return ((FileMetaData) obj).getFileName().equals(this.getFileName());
        }
        return  false;
    }
}
