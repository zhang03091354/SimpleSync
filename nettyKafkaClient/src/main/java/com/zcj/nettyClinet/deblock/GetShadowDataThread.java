package com.zcj.nettyClinet.deblock;

import com.zcj.nettyClinet.tools.FileLocation;
import com.zcj.nettyClinet.tools.SingletonNetty;
import com.zcj.nettyClinet.tools.SyncFileInfo;

import java.io.*;

public class GetShadowDataThread implements Runnable {
    @Override
    public void run() {
        SyncFileInfo fileName = SingletonNetty.INSTANCE.getClientCurrentFile();
        String srcFile = fileName.getSrcFileLocation();
        String shadowData = srcFile + ".sd";
        //String shadowData = "e:/1.sd";
        File file = new File(shadowData);
        if (!file.exists()) {
            // 文件不存在
            //不做处理，即hashmap中为空

        } else {
            InputStream fileIn = null;
            BufferedInputStream bufferIn = null;
            try {
                fileIn = new FileInputStream(shadowData);
                bufferIn = new BufferedInputStream(fileIn);
                int size = bufferIn.available();
                byte[] buffer = new byte[size];

                int readLength = bufferIn.read(buffer);
                if (readLength != size) {
                    SingletonNetty.INSTANCE.printClientMes("Error", "读取影子数据异常:" + shadowData);
                }

                String sdString = new String(buffer, SingletonNetty.One2OneCodeType);
                geneShadowDataMem(sdString);

                bufferIn.close();
                fileIn.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        SingletonNetty.getShadowDataThreadFlag = 1;
    }

    /**
     * 解析影子数据 格式为：fileIndex,blockLength,MD5,:;...,:;fileIndex,blockLength,MD5
     *
     * @param sdString
     * @throws UnsupportedEncodingException
     */
    public void geneShadowDataMem(String sdString) throws UnsupportedEncodingException {
        String[] sdList = sdString.split(",:;");//每一个元素为一个分块的摘要信息包括 起始下标，分块长度，分块MD5
        if (sdList.length == 0) {
            SingletonNetty.INSTANCE.printClientMes("Error", "解析影子数据异常:格式错误或者没有分块摘要信息！");
            return; //格式错误或者没有分块摘要信息。
        }
        for (String sr :
                sdList) {
            String a = sr.substring(sr.length() - 16, sr.length());
            sr = sr.substring(0, sr.length() - 16);
            String[] itemList = sr.split(",");
            if (itemList.length < 2) {
                SingletonNetty.INSTANCE.printClientMes("Error", "解析影子数据异常:单个分块摘要信息格式错误！");
                return; //格式错误或者没有分块摘要信息。
            }
            SingletonNetty.INSTANCE.AddClient_MD52Index(a, new FileLocation(Integer.parseInt(itemList[0]), Integer.parseInt(itemList[1])));
        }
    }
}
