package com.app.ingestion.util;

/**
 * Created by sgu197 on 9/24/2017.
 */
public class SftpConstants {

    public static final String FTP_REFRESH_STEP = "ftp.refresh.step";
    public static final String FTP_WAIT_CAP = "ftp.wait.cap";
    public static final String INGEST_TRANSFER_TOPIC = "ingest.transfer.topic";
    public static final String INGEST_ERROR_TOPIC = "ingest.error.topic";
    public static final String FTP_SUCCESS_TOPIC = "ingest.success.topic";
    public static final String AC_SFTP_SERVER_INPUT_HOST = "ac.sftp.server.input.host";
    public static final String AC_SFTP_SERVER_INPUT_USER = "ac.sftp.server.input.user";
    public static final String AC_SFTP_SERVER_INPUT_PORT = "ac.sftp.server.input.port";
    public static final String AC_SFTP_SERVER_INPUT_KEY_LOCATION = "ac.sftp.server.input.key.location";
    public static final String AC_SFTP_SERVER_INPUT_DIRECTORY = "ac.sftp.server.directory.input";



    public static final String AC_DIR_INPUT = "ac.dir.input";
    public static final String AC_FILE_INPUT = "ac.file.input";
    public static final String AC_DIR_ALLOWED_PATTERN = ".pattern.allowed";
    public static final String AC_DIR_SOURCE_NAME = ".source.name";
    public static final String AC_DIR_NOT_ALLOWED_PATTERN = ".pattern.notallowed";
    public static final String AC_DIR_FILE_IN_TRANSFER_PATTERN = ".pattern.file.in.transfer";
    public static final String AC_DIR_ASSET_CLASS = ".asset.class.name";
    public static final String SESSION_TIMEOUT = "sesion.timeout.millisec";


    public static final String AC_TASK_DIR_ID = "ac.task.dir.id";

    public static final String CHECKSUM_FLAG = "checksum.flag";
    public static final String FILE_READ_PERMISSION_FLAG = "file.read.permission.flag";
    public static final String MD5SUM_MAX_NUM_FILE = "md5sum.max.num.file";


    public static final String BACK_OFF_STEP = "backoff.step";
    public static final String BACK_OFF_CAP = "backoff.gap";
    public static final String RETRY_TIME_GAP = "retry.time.gap";
    public static final String NUMBER_OF_TRIES = "number.of.tries";
    public static final String AUTHENTICATION_METHOD = "authentication.method";
    public static final String KERBEROS_PRINCIPAL = "kerberos.principal";
    public static final String KEYTAB_FILE = "keytab.file";
    public static final String VALUE_SEPARATOR = ",";
    public static final String MASTER_TASK = "master.task";
    public static final String TASK_PATTERN = "task.pattern";
    public static final String TASK_HOST = "task.host";

    public static final String HDFS_TARGET_LOCATION = "hdfs.target.location";
    public static final String ARCHIVE_LOCATION = "archive.location";
    public static final String FILE_SEPARATOR = "/";





}
