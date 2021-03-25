package com.zcj.nettyClinet.deblock;

public interface DeblockFactory {
    enum CallType {
        SERVER, //服务器端调用
        CLIENT, //客户端调用
    }
    Boolean Deblock(CallType rt);
}
