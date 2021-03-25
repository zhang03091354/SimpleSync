package com.zcj.nettyServer.handleSync;

import com.zcj.nettyServer.tools.SingletonNetty;

import java.io.*;

public class TempHandleSync {
    private static FileOutputStream fileOut;  //这是为了生成新文件
    private static BufferedOutputStream dataOut;
    private static RandomAccessFile fileIn;

    public static void handleMes(String mes) throws IOException {
        String[] blockList = mes.split(SingletonNetty.splitOfBlock);
        int blockNum = blockList.length; //并不是实际的分块数，因为消息格式为 ： fileName分隔符0,index,length分隔符1,bytes分隔符0...
        if (blockNum < 2) {
            SingletonNetty.INSTANCE.printServerMes("Error", "同步请求的消息格式错误");
            return;
        }
        String fileBackup = "e://" + blockList[0] + ".bu";
        String newFile = fileBackup + ".new";

        fileOut = new FileOutputStream(newFile);
        dataOut = new BufferedOutputStream(fileOut);
        fileIn = new RandomAccessFile(fileBackup, "r");

        for (int i = 1; i < blockNum; i++) {
            String str = blockList[i];
            String[] strList = str.split(",");//按照 ， 分割
            if (strList.length == 0) {
                SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中单个分块的格式错误");
                return;
            }
            if (strList[0].equals("0")) {
                //相同块，直接从备份文件中读取内容
                if (strList.length != 3) {
                    SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中相同分块的格式错误");
                    return;
                }
                int index = Integer.parseInt(strList[1]);
                int length = Integer.parseInt(strList[2]);
                fileIn.seek((long) index);
                byte[] toWrite = new byte[length];
                int readByte = fileIn.read(toWrite);
                if (readByte != length) {
                    SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中相同分块在读取备份文件时发生读取字节不足的问题！");
                    return;
                }
                dataOut.write(toWrite);//写如新备份文件
            } else if (strList[0].equals("1")) {
                //差异块，直接从消息中获取内容
                if (strList.length < 2) {
                    SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中差异分块的格式错误");
                    return;
                }
                String bytesStr = str.substring(2,str.length());
                byte[] toWrite = bytesStr.getBytes(SingletonNetty.One2OneCodeType);
                dataOut.write(toWrite);//写如新备份文件
            } else {
                SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中单个分块的格式错误，没有注明是相同块还是差异块！");
                return;
            }
        }

        dataOut.flush();
        dataOut.close();
        fileOut.close();
        fileIn.close();

        return;
    }
}
