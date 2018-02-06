package com.app.ingestion.poller;

import com.app.ingestion.fileselector.FileSelector;
import com.app.ingestion.fileselector.FileSelectorType;
import com.app.ingestion.fileselector.impl.FileNamePatternSelector;
import com.app.ingestion.model.FileMetaData;
import com.app.ingestion.model.JschConnection;
import com.app.ingestion.validator.design.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Created by sgu197 on 9/25/2017.
 */
public class DirSftpPoller implements SourcePoller<FileMetaData>{
    private static Logger LOG = LoggerFactory.getLogger(DirSftpPoller.class);

    private FileSelector selector;
    private SourcePollerWithCRC internalPoller;
    private String dirPath;
    private FileSelectorType selectorType;
    private Map<String,String> props;
    private Validator validator;

    public void setJschConnection(JschConnection jschConnection) {
        this.jschConnection = jschConnection;
    }

    private JschConnection jschConnection;

    public DirSftpPoller(SourcePollerWithCRC poller,String dirPath,Map<String,String> props,Validator validator){

        this.internalPoller = poller;
        this.dirPath = dirPath;
        this.selector = new FileNamePatternSelector(validator);
        this.props = props;
        this.validator = validator;
    }


    public DirSftpPoller(String dirPath,FileSelectorType selectorType,Validator validator,Map<String,String> props,
                         JschConnection jschConnection){
        LOG.info("Creating DirSourcePoller");
        this.props = props;
        this.dirPath = dirPath.replace("\\","/");
        this.selectorType = selectorType;
        this.validator = validator;
        this.jschConnection = jschConnection;
    }


    //Adding getter and setter
    public JschConnection getJschConnection() {
        return this.jschConnection;
    }


    //this will overwrite existing connection - if such exists
    public void setSelector(FileSelector selector) {
        this.selector = selector;
    }

    public void initializePoller(){
        LOG.info("Initializing selector .....");
        if(selectorType.equals(FileSelectorType.FILENAME)){
            LOG.info("FileSelector TYPE is FILENAME");
            if(jschConnection != null && jschConnection.isValid()){
                internalPoller = new JschPoller(props,dirPath,jschConnection);
            }else{
                internalPoller = new JschPoller(props,dirPath);
            }
            selector =  new FileNamePatternSelector(validator);
        }else{
            LOG.warn(selectorType + "--File Selector does not exist");
        }
    }


    public void setSftpWorkingDir(String sftpWorkingDir){
        internalPoller.setSftpWorkingDir(sftpWorkingDir);
        LOG.info("Directory reset to "+sftpWorkingDir);
    }
    @Override
    public List<FileMetaData> poll() throws InterruptedException {
        try {
            return selector.filter(internalPoller.poll());
        }catch (Exception ex){
            LOG.error("Poller failed ",ex);
        }
        return null;
    }

    public List<FileMetaData> setMd5Checksum(List<FileMetaData> fileList){
        return internalPoller.populateMd5Checksum(fileList);
    }
    @Override
    public void stop() throws IOException {

        try{
            internalPoller.stop();
        }catch (Exception e){
            LOG.error("Stop failed for poller ",e);
        }
    }
}
