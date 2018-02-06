package com.app.ingestion.archiver;

import com.app.ingestion.model.JschConnection;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sgu197 on 9/26/2017.
 */
public class Archiver {

    private static Logger LOG = LoggerFactory.getLogger(Archiver.class);
    Map<String,String> props;
    public static final String ERROR = "exception";
    public static final String TRANSFERRED = "transferred";
    public static final String STAMP_TRANSFERRED_NOT_ARCHIVED = ".archiving_failure";
    public static final String STAMP_NOT_TRANSFERRED_NOT_ARCHIVED = ".transfer_and_archiving_failure";

    public static final String PERMISSION = "755";

    public Archiver(Map<String,String> props){
        this.props = props;
    }

    //String date = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).format(new Date()).toString();
    public boolean archive(JschConnection connection,String fileName,String assetClass,String sourceName,
                           String sourceDir,String date,
                           String status,
                           String permission,
                           String archivingDir){


        Map<String,String> file = new HashMap<>();

        file.put("SRC",sourceDir);
        file.put("fileName",fileName);
        file.put("DST",getPath(date,assetClass,sourceName,status));
        file.put("SRCDataSet",sourceName);

        return mv_file(connection,file,permission,archivingDir);
    }

    public String getPath(String date,String assetClass,String sourceName,String status){
        String DST = date.substring(0,4) + "/" +date.substring(4,6) + "/"+ date.substring(6,8)+"/"+assetClass+"/"+sourceName+"/"+status;
        return DST;
    }

    private boolean mv_file(JschConnection hostConnection,Map<String,String> file,String permissions,String archivingDir) {
        String dst = findOrCreateDirectory(hostConnection, file, archivingDir);
        int permission = Integer.parseInt(permissions, 8);

        boolean fileArchived = false;
        boolean permissionChanged = false;
        try {
            hostConnection.getChannelSftp().getHome();
            hostConnection.getChannelSftp().rename(file.get("SRC") + "/" + file.get("fileName"), dst + "/" + file.get("fileName"));
            fileArchived = true;
            hostConnection.getChannelSftp().chmod(permission, dst + "/" + file.get("fileName"));
            permissionChanged = true;
            LOG.debug("archived " + dst + "/" + file.get("fileName"));
            return true;

        } catch (SftpException e) {
            if(fileArchived&&
                    !permissionChanged)
                LOG.debug("File Archived but could not change permission on destination");
            else
                LOG.debug("!!!Couldn't archive file: "+file.get("SRC"));
            return false;
        }
    }

        public String findOrCreateDirectory(JschConnection hostConnection,Map<String,String> file,String archivingDir) {

            LOG.debug("trying to find/create destination path");
            String[] children = file.get("DST").split("/");
            try {
                hostConnection.getChannelSftp().cd(archivingDir);
                return findOrCreate(hostConnection, archivingDir, children, 0);
            } catch (SftpException e) {
                LOG.error("destination directory is not available");
                return null;
            }
        }


        public String findOrCreate(JschConnection hostConnection,String parent,String[] children,int n)
        throws SftpException{

            if(n >= children.length) {
                LOG.debug("destination path established, returning: " + parent);
                return parent;
            }

            hostConnection.getChannelSftp().cd(parent);
            if(isValidDir(hostConnection,hostConnection.getChannelSftp().pwd()+"/"+children[n])) {
                return findOrCreate(hostConnection, hostConnection.getChannelSftp().pwd() + "/" + children[n],
                        children, n + 1);
            }else {
                String newDir = makeDir(hostConnection,parent,children[n]);
                return findOrCreate(hostConnection,newDir,children,n+1);
            }

     }

    //made public for testing purposes

    public Boolean isValidDir(JschConnection hostConnection,String path){

        LOG.debug("validating directory "+path);
        try{

            SftpATTRS attr = hostConnection.getChannelSftp().stat(path);
            if(attr!=null && attr.isDir()){
                LOG.debug("directory "+path + " is valid: "+Boolean.TRUE);
                return Boolean.TRUE;
            }
        }catch (Exception e){

        }

        LOG.debug("directory "+path+" is valid: "+ Boolean.FALSE);
        return Boolean.FALSE;

    }

    //made public for testing purposes
    public String makeDir(JschConnection hostConnection,String parent,String child) throws SftpException{

        hostConnection.getChannelSftp().mkdir(parent+"/"+child);
        hostConnection.getChannelSftp().cd (parent+ "/" +child);
        LOG.debug("created new directory: "+hostConnection.getChannelSftp().pwd());
        return hostConnection.getChannelSftp().pwd();

    }

    public void delete(File path){
        File[] l = path.listFiles();
        for (File f : l){
            if (f.isDirectory())
                delete(f);
            else
                f.delete();
        }
        path.delete();
    }


    public static void main(String[] args){
       Archiver archiver = new Archiver(new HashMap<String,String>());

       archiver.delete(new File("C:\\Users\\sgu197\\archiver\\sftp"));
    }
}
