package com.zcj.nettyClinet.tools;

import java.io.UnsupportedEncodingException;

public class BlockInfoSrc {
    private byte[] md5;
    private int fileIndex;
    private int blockLength;
    private byte[] bytes;

    public BlockInfoSrc(){}
    public BlockInfoSrc(byte[] md5, int fileIndex, int blockLength, byte[] bytes){
        this.md5 = md5;
        this.fileIndex = fileIndex;
        this.blockLength = blockLength;
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        try {
            String ss = this.fileIndex+","+this.blockLength+""+new String(this.md5,SingletonNetty.One2OneCodeType);
            return ss;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public byte[] getMd5() {
        return md5;
    }

    public void setMd5(byte[] md5) {
        this.md5 = md5;
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

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
