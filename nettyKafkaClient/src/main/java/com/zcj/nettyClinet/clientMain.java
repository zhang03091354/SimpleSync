package com.zcj.nettyClinet;

import com.zcj.nettyClinet.tools.LinkedQueue;
import com.zcj.nettyClinet.tools.SingletonNetty;
import com.zcj.nettyClinet.tools.SyncFileInfo;

import static java.lang.Thread.sleep;

public class clientMain {
    public static void main(String[] args) {
        clientImp cli = new clientImp();
        LinkedQueue<SyncFileInfo> fileList = new LinkedQueue<>();
        fileList.initQueue();

        try {
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String src = "e://1.ss";//"e://1.ss"; // test0_Insert.txt
        String backup = "e://2.s"; //1.ss test0.txt  当前系统中backup文件没有用，在服务器端用的同名文件
        fileList.push(new SyncFileInfo(src, SingletonNetty.INSTANCE.TOTALFILE,backup));
        //fileList.push(new SyncFileInfo("e://2.pptx", SingletonNetty.INSTANCE.TOTALFILE,backup));

        cli.startClient(fileList,"192.168.1.205",7788);
    }
}
