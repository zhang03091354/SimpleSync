package com.zcj.nettyServer.tools;


public class SrcDiffChecksumType {
    private Boolean isLast;
    private String isSame;
    private String blockInfo;
    byte[] bytes;

    public SrcDiffChecksumType(){
        isLast = false;
        isSame = "same";
        blockInfo = "";
    }
}
