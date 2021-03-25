package com.zcj.nettyServer;

public class serverMain {
    public static void main(String[] args) {
        serverImp ser = new serverImp();
        ser.startNettyServer("127.0.0.1",7788);
    }

}
