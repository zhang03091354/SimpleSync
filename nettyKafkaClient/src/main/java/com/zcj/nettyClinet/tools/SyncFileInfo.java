package com.zcj.nettyClinet.tools;


public class SyncFileInfo {
    private String srcFileLocation;
    private String backupFileLocationType;
    private String backupFileLocation;
    {
        srcFileLocation = "Test data: srcFileLocation";
        backupFileLocationType="";
        backupFileLocation = "Test data: backupFileLocation";
    }

    public SyncFileInfo(){

    }

    public SyncFileInfo(String srcFileLocation, String backupFileLocationType, String backupFileLocation){
        this.srcFileLocation = srcFileLocation;
        this.backupFileLocation = backupFileLocation;
        this.backupFileLocationType = backupFileLocationType;
    }


    public String toString(){
        String ret = "";
        ret = ret+"[源文件："+srcFileLocation+"]-------[备份文件："+backupFileLocation+"]";
        return ret;
    }

    public String getSrcFileLocation() {
        return srcFileLocation;
    }

    public void setSrcFileLocation(String srcFileLocation) {
        this.srcFileLocation = srcFileLocation;
    }

    public String getBackupFileLocationType() {
        return backupFileLocationType;
    }

    public void setBackupFileLocationType(String backupFileLocationType) {
        this.backupFileLocationType = backupFileLocationType;
    }

    public String getBackupFileLocation() {
        return backupFileLocation;
    }

    public void setBackupFileLocation(String backupFileLocation) {
        this.backupFileLocation = backupFileLocation;
    }
}
