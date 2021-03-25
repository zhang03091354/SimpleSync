package com.zcj.nettyClinet.tools;

public class FileLocation {
    private int fileIndex;
    private int blockLength;

    public FileLocation(){}
    public FileLocation(int fileIndex, int blockLength){
        this.fileIndex = fileIndex;
        this.blockLength = blockLength;
    }

    public int getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(int fileIndex) {
        this.fileIndex = fileIndex;
    }

    public int getBlockLength() {
        return blockLength;
    }

    public void setBlockLength(int blockLength) {
        this.blockLength = blockLength;
    }
}
