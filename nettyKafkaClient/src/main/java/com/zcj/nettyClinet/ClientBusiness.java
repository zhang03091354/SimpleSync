package com.zcj.nettyClinet;

import com.zcj.nettyClinet.deblock.DeblockFactory;
import com.zcj.nettyClinet.deblock.DeblockOfPCI;
import com.zcj.nettyClinet.deblock.GetShadowDataThread;
import com.zcj.nettyClinet.tools.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static java.lang.Thread.sleep;

/**
 * @Author zcj
 * 这是主线程逻辑类
 */
public class ClientBusiness {

    //客户端的业务逻辑
    public static void serviceCode() throws InterruptedException {

        Boolean flag = true;
        while (flag) {
            sleep(1); //交出cpu控制权
            if (!SingletonNetty.INSTANCE.getFileList().isEmpty()) {
                //SyncFileInfo head = SingletonNetty.INSTANCE.getFileList().getHead();

                clearTempInfo();
                handleOneFile();

            } else {
                SingletonNetty.INSTANCE.printClientMes("Prompt infomation", "文件列表中已经没有数据！ 任务结束！");
                //SingletonNetty.INSTANCE.getClientCtx().close();
                flag = false;
            }

        }
    }

    /**
     * 处理完一个文件后，需要将该文件的一些临时信息情况
     * 比如：该文件对应的影子数据的解析结果链表，
     * 该文件对应的分块摘要信息链表，
     */
    public static void clearTempInfo() {
        SingletonNetty.INSTANCE.getSrcBlockList().clear();
        SingletonNetty.INSTANCE.getClient_MD52Index().clear();
        SingletonStatistics.INSTANCE.blockCountClear();
    }

    public static void handleOneFile() {
        SyncFileInfo fileInfo = SingletonNetty.INSTANCE.getFileList().pull();
        SingletonNetty.INSTANCE.setClientCurrentFile(fileInfo);
        SingletonNetty.INSTANCE.printClientMes("Prompt infomation",
                "客户端开始生成一次同步请求：" + SingletonNetty.INSTANCE.getClientCurrentFile());

        SingletonNetty.INSTANCE.printClientMes("time","开始处理文件");
        getShadowData();
        deblockFile();
        getDiffInfo();
        //已经发送消息
        SingletonNetty.INSTANCE.printClientMes("time","发出同步请求");
        updateShadowData();

        SingletonStatistics.INSTANCE.timerForTotal_Start();
        SingletonStatistics.INSTANCE.timerForTest_Start();
    }

    public static void getShadowData() {
        //起一个单独线程去处理影子数据的读取
        SingletonNetty.getShadowDataThreadFlag = 0;//等于0表示还在读取影子数据。
        GetShadowDataThread getShadowDataThread = new GetShadowDataThread();
        Thread thread = new Thread(getShadowDataThread);
        thread.start();
    }

    public static void deblockFile() {
        DeblockFactory db = new DeblockOfPCI();
        db.Deblock(DeblockFactory.CallType.CLIENT);
    }

    /**
     * 差异信息格式为：backupFileName分隔符0,fileIndex,blockLength分隔符...分隔符1,bytes分隔符...
     */
    public static void getDiffInfo() {
        //此时，已经完成了分块过程，但不确定是否完成了读取影子数据的过程
        while (SingletonNetty.getShadowDataThreadFlag == 0) {
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }//阻塞等待,跳出则表示已经完成了影子数据的读取
        StringBuilder mesBuffer = new StringBuilder();
        FileOutputStream fileOut = null;
        String fileSDB = SingletonNetty.INSTANCE.getClientCurrentFile().getSrcFileLocation() + ".sdb";//新产生的影子数据
        String fileName = getFilename(SingletonNetty.INSTANCE.getClientCurrentFile().getSrcFileLocation());//这个名字是存储在服务器端的名字
        mesBuffer.append(fileName);//在同步请求中，先加入备份文件的名字
        int firstflag = 0;//第一个分块信息，在生产新的影子数据时前面不需要加分号
        int lastBlockType = 0; //上一个分块的类型 0表示重复块  1表示差异块
        HashMap<String, FileLocation> client_md52Index = SingletonNetty.INSTANCE.getClient_MD52Index();
        try {
            fileOut = new FileOutputStream(fileSDB);
            BufferedOutputStream dataOut = new BufferedOutputStream(fileOut);
            LinkedQueue<BlockInfoSrc> srcBlockList = SingletonNetty.INSTANCE.getSrcBlockList();
            while (!srcBlockList.isEmpty()) {
                BlockInfoSrc item = srcBlockList.pull();
                String newSDBOfEachBlock = item.toString();
                //写新的影子数据
                if (firstflag == 0) {
                    firstflag = 1;
                    dataOut.write(newSDBOfEachBlock.getBytes(SingletonNetty.One2OneCodeType));
                } else {
                    dataOut.write((",:;" + newSDBOfEachBlock).getBytes(SingletonNetty.One2OneCodeType));
                }
                //生产同步请求消息
                String snode = new String(item.getMd5(), SingletonNetty.One2OneCodeType);
                if (client_md52Index.containsKey(snode)) {
                    //重复分块
                    FileLocation fileLocation = SingletonNetty.INSTANCE.getClient_MD52Index().get(snode);
                    mesBuffer.append(SingletonNetty.splitOfBlock + "0," + fileLocation.getFileIndex() + "," + fileLocation.getBlockLength());
                    lastBlockType = 0;
                } else {
                    //差异分块
                    if (lastBlockType == 0) {
                        mesBuffer.append(SingletonNetty.splitOfBlock + "1,");
                    }
                    mesBuffer.append(new String(item.getBytes(), SingletonNetty.One2OneCodeType));
                    lastBlockType = 1;
                }
            }

            String sendMes = mesBuffer.toString();
            sendSyncMes(sendMes);
            SingletonNetty.INSTANCE.printClientMes("Prompt infomation", "已经发送同步请求");

            //dataOut.write(byteOut.toByteArray());
            dataOut.flush();
            dataOut.close();
            fileOut.close();
            SingletonNetty.INSTANCE.printClientMes("Prompt infomation", "已经生产新的影子数据");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateShadowData() {
        File oldFile = new File(SingletonNetty.INSTANCE.getClientCurrentFile().getSrcFileLocation() + ".sd");
        File newFile = new File(SingletonNetty.INSTANCE.getClientCurrentFile().getSrcFileLocation() + ".sdb");
        if (!newFile.exists()) {
            SingletonNetty.INSTANCE.printClientMes("Error infomation", "替换影子数据时，没有找到新的影子数据");
            return;
        }
        if (oldFile.exists()) {
            //SingletonNetty.INSTANCE.printClientMes("Prompt infomation","替换影子数据时，没有找到旧的影子数据");
            boolean delete = oldFile.delete();
            if (delete) {
                SingletonNetty.INSTANCE.printClientMes("Prompt infomation", "删除旧影子数据成功！");
            } else {
                SingletonNetty.INSTANCE.printClientMes("Error infomation", "删除旧影子数据失败！");
            }
        }
        boolean b = newFile.renameTo(oldFile);
        if (b) {
            SingletonNetty.INSTANCE.printClientMes("Prompt infomation", "替换影子数据成功！");
        } else {
            SingletonNetty.INSTANCE.printClientMes("Error infomation", "替换影子数据失败！");
        }
    }

    public static void sendSyncMes(String mes) {
        SingletonNetty.INSTANCE.getClientCtx().writeAndFlush(mes);
    }

    public static String getFilename(String srcLocation) {
        String fileName = "";
        String[] te = srcLocation.split("/");
        fileName = te[te.length - 1];
        return fileName;
    }

    private static void errorProcess() {
        //处理出错的情况 TODO
        //Singleton.INSTANCE.printClientMes("Prompt infomation","【errorProcess】通讯异常！！！");
        return;
    }

}
