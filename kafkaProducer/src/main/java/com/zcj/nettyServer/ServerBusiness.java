package com.zcj.nettyServer;

import com.zcj.nettyServer.handleSync.TempHandleSync;
import com.zcj.nettyServer.tools.SingletonNetty;
import org.apache.kafka.clients.producer.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @Author zcj
 * 这是主线程逻辑类
 */
public class ServerBusiness {
    //服务器的业务逻辑
    public static void serviceCode() throws InterruptedException {

        while (true){
            Thread.sleep(1); //交出cpu控制权
            while (!SingletonNetty.INSTANCE.nettyMesList.isEmpty()){
                String temp = SingletonNetty.INSTANCE.nettyMesList.poll();
                SingletonNetty.INSTANCE.kafkaSendMes(temp); //.substring(0,20)
                SingletonNetty.INSTANCE.printServerMes("Prompt info","向kafka中写入消息:"+temp.substring(0,20)+"...");

//                try {
//                    TempHandleSync.handleMes(temp);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }


    private static void errorProcess(){
        //处理出错的情况 TODO
        //Singleton.INSTANCE.printServerMes("Prompt infomation","【errorProcess】通讯异常！！！");
        SingletonNetty.INSTANCE.setServerCurrentState(SingletonNetty.ProcessState.SERVER_AND_CLIENT_IDLE);
        return;
    }

}
