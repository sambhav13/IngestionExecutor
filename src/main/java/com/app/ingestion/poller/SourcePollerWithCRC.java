package com.app.ingestion.poller;

import com.app.ingestion.model.FileMetaData;

import java.util.List;

/**
 * Created by sgu197 on 9/25/2017.
 */
public interface SourcePollerWithCRC<T> extends SourcePoller<T> {

     List<FileMetaData> populateMd5Checksum(List<FileMetaData> fileList);

     void setSftpWorkingDir(String sftpWorkingDir);
}
