package com.app.ingestion.nfs;

import com.app.ingestion.exception.IngestionException;
import com.app.ingestion.model.FileMetaData;
import com.app.ingestion.model.JschConnection;
import com.app.ingestion.util.StopWatch;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.sun.deploy.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.omg.SendingContext.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


/**
 * Created by sgu197 on 9/25/2017.
 */
public class NFSUtil {

    private static Logger log = LoggerFactory.getLogger(NFSUtil.class);

    public static String getTargetDir(String hdfsLocation,String sourceName,String assetClass,String date){

        if((hdfsLocation!=null && hdfsLocation.length()> 0) && (assetClass!=null && assetClass.length()>0)
                &&(sourceName!=null && sourceName.length() >0)){
            if(hdfsLocation.charAt(hdfsLocation.length()-1) != '/'){
                hdfsLocation = hdfsLocation + "/";
            }

            hdfsLocation = hdfsLocation
                    .replace("${sourceName}",sourceName)
                    .replace("${assetClass}",assetClass);

            log.debug("The hdfs location after is -->"+hdfsLocation);

        }else{
            log.debug("INVALID ARGUMENTS PROVIDED" + hdfsLocation+ " "+assetClass+ " "+sourceName);
        }
        return hdfsLocation;
    }

    public static String getTargetDestPath(String hdfsLocation,String fileName){

        String hdfsPath = hdfsLocation + fileName;
        File f =  new File(hdfsPath);
        if(f.exists()){
            hdfsPath = addTimeStampToFile(hdfsPath);
        }

        return hdfsPath;
    }

    public static void saveFileToNFS(JschConnection jschConnection, String filePath, FileMetaData fileMetaData,
                                     String hdfsLocation,Boolean useChecksum,int numOfTies,int retryTimeGap)
    throws IOException,InterruptedException,IngestionException{

        StopWatch stopWatch = new StopWatch();

        filePath = filePath.replace("\\","/");
        if(filePath != null && filePath.length() >0 && filePath.charAt(filePath.length()-1) != '/'){
            filePath = filePath + "/";
        }

        String fileName = fileMetaData.getFileName();
        String md5checksum = fileMetaData.getMd5Checksum();

        String localSrc = filePath + fileName;
        String dst =  hdfsLocation+fileName;


        InputStream in = null;
        String checksum = "";

        OutputStream out = null;

       // File file = new File(dst);
        //file.getParentFile().mkdirs();
        //out = new FileOutputStream(file);

        int inRetryAttempts = numOfTies;
        while(inRetryAttempts>0){
            try{
                in = jschConnection.getChannelSftp().get(localSrc);
                break;
            }catch (SftpException sftpE){
                if(inRetryAttempts==0){
                    String msg = "UNABLE TO GET INPUT STREAM";
                    log.error(msg);
                    throw new IngestionException(msg);
                }
            }
            Thread.sleep(10000);
        }

        //if(in ==null || out == null){
        if(in ==null ){
            String msg = "UNABLE TO GET INPUT/OUTPUT STREAM";
            log.error(msg);
            throw new IOException(msg);

        }

        //ONCE WE HAVE STREAMS SET
        if(!useChecksum){
            log.debug("inside not use checksum");
            //copyBytes(in,out,4096);
            try {
                if(!isDirExist(jschConnection,hdfsLocation)){
                    String[] directories = hdfsLocation.split("/");
                    StringBuffer basedDir = new StringBuffer("");

                    basedDir.append("/");
                    for(int  i = 0;i<directories.length-2;i++){
                        basedDir.append(directories[i]);

                            basedDir.append("/");

                    }
                    jschConnection.getChannelSftp().mkdir(basedDir+directories[directories.length-2]);
                    jschConnection.getChannelSftp().mkdir(basedDir+
                            "/"+directories[directories.length-2]+"/"+directories[directories.length-1]);
                }
                //out = jschConnection.getChannelSftp().put( dst+".writing");
                ChannelSftp nc = jschConnection.getNewChannel();
                out = nc.put( dst+".writing");
                copyBytes(in,out,4096);
                nc.rename(dst+".writing",dst);
                jschConnection.closeNewChannel();
                /*out = jschConnection.getNewChannel().put( dst+".writing");
                copyBytes(in,out,4096);
                jschConnection.getChannelSftp().rename(dst+".writing",dst);*/
               // jschConnection.getChannelSftp().rm(localSrc);
            }catch (SftpException se){
                log.error("not able to copy file to dest: "+dst);
                se.printStackTrace();
            }

            //in.close();
            log.info("Successfully copied file from "+ localSrc + " to "+dst);
            //out.close();
        } else {
            log.debug("inside use checksum");
            try {

                if(!isDirExist(jschConnection,hdfsLocation)){
                    String[] directories = hdfsLocation.split("/");
                    StringBuffer basedDir = new StringBuffer("");

                    basedDir.append("/");
                    for(int  i = 0;i<directories.length-2;i++){
                        basedDir.append(directories[i]);

                        basedDir.append("/");

                    }
                    jschConnection.getChannelSftp().mkdir(basedDir+directories[directories.length-2]);
                    jschConnection.getChannelSftp().mkdir(basedDir+
                            "/"+directories[directories.length-2]+"/"+directories[directories.length-1]);
                }
                ChannelSftp nc = jschConnection.getNewChannel();
                out = nc.put( dst+".writing");
                //copyBytes(in,out,4096);
                checksum = copyBytesAndReturnAndChecksum(in, out, 4096, true, numOfTies, retryTimeGap);
                nc.rename(dst+".writing",dst);
                jschConnection.closeNewChannel();

                //out.close();
            } catch (IOException ioE) {
                String msg = "ERROR while copying bytes with checksum";
                log.error(msg);
                throw new IngestionException(msg);
            } catch (SftpException e) {
                e.printStackTrace();
            }

            if (checksum.length() > 0) {
                log.debug("The checksum calculated for hdfs file " + dst + " is " + checksum);
                log.debug("The checksum calculated for source file " + fileName + " is " + md5checksum);

                //Metch MessageDigest
                if (checksum.equals(md5checksum)) {
                    log.debug("checksum Matched for file " + fileName);
                    log.debug("Successfully copied file from " + localSrc + " to " + dst);
                } else {
                    String msg = "File Transfer Failure - CheckSum Mismatch";
                    log.debug(msg);
                    throw new RuntimeException(msg);
                }
            }
        }

        //out.close();
        log.debug("NFR-Write file in HDFS{}"+"- Execution took in seconds: {}",dst,stopWatch.getElapsedTime());

    }

    public static void moveFileToNFS(JschConnection jschConnection, String dirPath, FileMetaData fileMetaData,
                                     String destPath,Boolean useChecksum,int retryAttempt,int retryTimeGap){



    }


    public static void copyBytes(InputStream in,OutputStream out,int bufferSize) throws IOException,IngestionException{
        try{
            IOUtils.copy(in,out,bufferSize);
            in.close();
            out.close();
        }catch(IOException io){
            log.error(io.getMessage());
            throw new IngestionException(io.getMessage());
        }
    }

    public static final String addTimeStampToFile(final String fileName){
        int extension = fileName.lastIndexOf(".");
        if(extension == -1){
            return fileName  + System.currentTimeMillis();
        }else{
            String filePrefix = fileName.substring(0,fileName.lastIndexOf("."));

            String fileExtension = fileName.substring(fileName.lastIndexOf("."),
                    fileName.length());
            return  filePrefix + "." + System.currentTimeMillis() + fileExtension;
        }
    }



    public static final String getMD5String(final byte[] digest){
        StringBuffer result = new StringBuffer("");
        for(int i=0;i<digest.length;i++){
            result.append(Integer.toString((digest[i]
                                             &
                                            0xff)
                                             +
                                            0x100,
                                            16).substring(1));
        }
        return result.toString();

    }


    public static String removeHdfsJunkFile(String dst,JschConnection jschConnection){
        boolean removeFlag = false;
        try{
            log.debug("Removing File {} from L1 Staging due to unsuccessful attempt",dst);
             jschConnection.getChannelSftp().rm(dst);
             removeFlag = true;
        } catch (SftpException e) {
            e.printStackTrace();
            log.error("Not able to delete location file"+dst);
            log.error("removal of file "+dst + " failed ",e);
            return "removal of junk file unsuccessful";
        }
        if(removeFlag)
            return " | file " + dst + "removed successfully";
        else
            return " | removal unsuccessful | Hadoop API did not remove file";
    }


    public static final String copyBytesAndReturnAndChecksum(final InputStream in,
                                                             final OutputStream out,
                                                             final int buffSize,
                                                             final boolean close,
                                                             int numOfTries,
                                                             long retryTimeGap) throws IOException {
        byte[] checksumbytes = null;
        String checksumString = null;
        boolean alreadyClosedInputStream = false;
        boolean alreadyClosedOutputStream = false;
        try{
            checksumbytes = copyBytes(in,out,buffSize,numOfTries,retryTimeGap);
            checksumString = getMD5String(checksumbytes);
            if(close){
                out.close();
                alreadyClosedOutputStream = true;
                in.close();
                alreadyClosedInputStream = true;
            }
        }finally {
            {
                if(close){
                    if(!alreadyClosedOutputStream){
                        out.close();
                    }
                    if(!alreadyClosedInputStream){
                        in.close();
                    }
                }
            }
        }

    return checksumString;
    }

    public static final byte[] copyBytes(final InputStream in,
                                        final OutputStream out,
                                        final int buffSize,int numOfTries,
                                        long retryTimeGap){

        byte[] checksumBytes = null;
        boolean transferSuccessFull = true;
        String exceptionMessage = "";

        while(true){
            PrintStream ps = out instanceof PrintStream ? (PrintStream)out : null;
            byte[] buf =  new byte[buffSize];
            MessageDigest complete = null;
            try{
                complete = MessageDigest.getInstance("MD5");

            }catch (NoSuchAlgorithmException ne){
                log.error("md5sum not found in sftp server");
            }


            try {
                for (int bytesRead = in.read(buf);
                     bytesRead >= 0; bytesRead = in.read(buf)) {

                    if (bytesRead > 0) {
                        complete.update(buf, 0, bytesRead);
                    }
                    out.write(buf, 0, bytesRead);
                    if (ps != null && ps.checkError()) {
                        log.error("Unable to write"
                                +
                                "to output stream.");
                        throw new IOException("Unable to write "
                                +
                                "to output stream");
                    }
                }
                transferSuccessFull = true;

            }catch(IOException io){
                log.error("IO Exception while copying bytes",io);
                transferSuccessFull = false;
                if(--numOfTries==0){
                    if(io.getMessage().toLowerCase().contains("refused")){
                        exceptionMessage = "SFTP server down";
                    }else if(io.getMessage().toLowerCase().contains("timeout")){
                        exceptionMessage = "Network Failure";
                    }else{
                        exceptionMessage = io.getMessage();
                    }
                    break;
                }
                try{
                    Thread.sleep(retryTimeGap);

                }catch (InterruptedException ie){
                    log.error("threadinterruption Exception",ie);
                    ie.printStackTrace();
                }
            }
            if(complete!=null){
                checksumBytes = complete.digest();
            }
            if(transferSuccessFull){
                break;
            }

        }

        if(!transferSuccessFull){
            log.error("JSCH connection exception: "
                        +
                        exceptionMessage);
            throw new RuntimeException("JSCH connection exception"
                                    +
                                    exceptionMessage);

        }
        return checksumBytes;

    }

    public static boolean isDirExist(JschConnection jschConnection,String hdfsLocation) throws SftpException{
        try {
            SftpATTRS attrs = jschConnection.getChannelSftp().lstat(hdfsLocation);
        }catch (SftpException se){
            log.info("directory path is not created ,hence creating path: "+hdfsLocation);
            return false;
        }
        return true;
    }
}
