package com.app.ingestion.fileselector;

import com.app.ingestion.model.FileMetaData;

import java.util.List;

/**
 * Created by sgu197 on 9/25/2017.
 */
public interface FileSelector {

    List<FileMetaData> filter(List<FileMetaData> list) throws InterruptedException;


}
