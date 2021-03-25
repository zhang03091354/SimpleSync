package com.zcj.nettyClinet.deblock;


import com.zcj.nettyClinet.tools.BlockInfoSrc;
import com.zcj.nettyClinet.tools.Hashcode;
import com.zcj.nettyClinet.tools.SingletonNetty;
import com.zcj.nettyClinet.tools.SingletonStatistics;

import java.io.*;

public class DeblockOfPCI implements DeblockFactory {

    public DeblockOfPCI(){}


    private static final int ARRAYSIZE = 8192;
    private String fileName;
    private int lenOfWindow;
    private int[] valuesInWindow;
    private int[] parityCheck;//一个字节包含多少个1
    private int thresholdVaule;
    private int indexOfBytesRead; //记录读取的字节数组中,已经保存到哪个位置
    private int blockIndex; //分块个数
    private ByteArrayOutputStream byteOut; //字节流中间缓存区
    private int fileIndex; //文件下标
    private int blockStartFileIndex; //分块的首字节所处文件的下标位置

    //写中间数据到磁盘
    private FileOutputStream fileOut;
    private BufferedOutputStream dataOut;

    private final String[] HexStringTable = new String[]
            {
                    "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
                    "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
                    "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
                    "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
                    "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
                    "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
                    "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
                    "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
                    "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
                    "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
                    "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
                    "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
                    "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
                    "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
                    "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
                    "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"
            };

    {
        lenOfWindow = ParaForDeblock.INSTANCE.getPci_windowSize();
        thresholdVaule = ParaForDeblock.INSTANCE.getPci_threshold();
        valuesInWindow = new int[lenOfWindow+1];
        parityCheck = new int[256];
        indexOfBytesRead = 0;
        blockIndex = 0;
        byteOut = new ByteArrayOutputStream(100000);
        byteOut.reset();
        fileIndex = 0;
        blockStartFileIndex = 0;



        for(int i=0;i<256;i++){
            for(int j=0;j<8;j++){
                int temp1 = i>>j;
                int temp2 = temp1&0x01;
                parityCheck[i] += temp2;
            }
        }
    }

    @Override
    public Boolean Deblock(CallType rt) {
        fileName = SingletonNetty.INSTANCE.getClientCurrentFile().getSrcFileLocation();

        try {
            InputStream fileIn = new FileInputStream(fileName);
            BufferedInputStream bufferIn = new BufferedInputStream(fileIn);
            deBlockFromInputBuffer(bufferIn, rt);
            bufferIn.close();
            fileIn.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void deBlockFromInputBuffer(BufferedInputStream in, CallType rt) throws IOException {

        byte[] bytesRead = new byte[ARRAYSIZE]; //与BufferedInputStream保持一致，可以最优化读取速度
        int readLength = -1;
        int byteRead = -1;
        int i = 0;

        //PCI特殊
        int bytesNumInArray = 0; //数组内的字节数
        int indexOfArray = 0; //数组内数据下标，指向下一个要插入的位置
        int numOfParity = 0; //数组内 1 的个数

        while ((readLength = in.read(bytesRead)) != -1){

            for (i = 0; i < readLength; i++) {
                byteRead = bytesRead[i];

                valuesInWindow[indexOfArray]=parityCheck[byteRead & 0xff];  //新读入的字节的奇偶校验值存入循环队列中
                indexOfArray++;  //指向下一个要写入的下标
                indexOfArray = indexOfArray>lenOfWindow?indexOfArray-lenOfWindow-1:indexOfArray;//indexOfArray = indexOfArray%(lenOfWindow+1);
                //更新奇偶校验值，计算窗口内的所有字节中的位中，包含1的个数
                if(bytesNumInArray<lenOfWindow){
                    numOfParity+=parityCheck[byteRead & 0xff];
                    bytesNumInArray++;
                }
                else{
                    //int temp1 = valuesInWindow[(indexOfArray+lenOfWindow)%(lenOfWindow+1)];
                    int temp2 = valuesInWindow[indexOfArray];
                    numOfParity=numOfParity+parityCheck[byteRead & 0xff]-temp2;
                }
                if(numOfParity>=thresholdVaule&&bytesNumInArray>=lenOfWindow){
                    //找到新切点
                    //System.out.printf("Time to deBlock with PCI block %d: %d 个字节.\n",blockIndex++,fileIndex-startIndex+1);
                    handleNewBlock(bytesRead,i,rt);

                    numOfParity=0;
                    bytesNumInArray=0;
                }
                fileIndex++;
            } //end for
            handleArrayFinish(bytesRead,i-1);
        } //end while

        handleLastBlock(rt);
        flushAndClose();

        SingletonNetty.INSTANCE.printClientMes("Prompt infomation","Deblock接口调用结束。。。");
        return ;
    }

    /**
     * 发现新块的处理逻辑，主要是保存分块信息
     * @param array 当前正在处理的byte数组
     * @param index 当前已经读取到@array中的位置，下次读取从index+1开始读取
     */
    private void handleNewBlock(byte[] array, int index, CallType rt){
        this.byteOut.write(array,this.indexOfBytesRead,index+1-this.indexOfBytesRead);
        byte[] bytes = byteOut.toByteArray(); //这个bytes即为一个分块的数据 处理2G数据分块时，此行代码耗时2秒左右
        byteOut.reset();

        blockSave(this.blockStartFileIndex,bytes,rt);

        SingletonStatistics.INSTANCE.blockCountAdd();
        this.blockIndex++;  //分块个数加一
        this.indexOfBytesRead = index+1;
        this.blockStartFileIndex = fileIndex + 1; //下一个分块的起始位置
    }

    /**
     * 一个缓冲数组结束，但是没有找到分块，需要将该缓冲数组中的剩余数据存入下一个分块数组中
     * @param array
     * @param index
     */
    private void handleArrayFinish(byte[] array, int index){
        this.byteOut.write(array,this.indexOfBytesRead,index+1-this.indexOfBytesRead);
        this.indexOfBytesRead = 0;
    }

    /**
     * 处理最后一个分块，该分块并不是因为找到切点，而是因为文件结束
     * @param rt
     */
    private void handleLastBlock(CallType rt) {
        if (this.byteOut.size() != 0){
            byte[] bytes = byteOut.toByteArray(); //这个bytes即为一个分块的数据
            byteOut.reset();

            blockSave(this.blockStartFileIndex,bytes,rt);

            SingletonStatistics.INSTANCE.blockCountAdd();
            this.blockIndex++;  //分块下标加一
            this.blockStartFileIndex = fileIndex+1; //下一个分块的起始位置,其实已经结束了

            System.out.println("src链表大小："+SingletonNetty.INSTANCE.getSrcBlockList().getLength());
            SingletonStatistics.INSTANCE.blockCountPrint();
        }
        //blockSave(-1,null,rt);//表示文件结束，方便后续环节识别文件结束与否
        return;
    }

    private void flushAndClose(){
//        if (SingletonStore.getSTORETYPE() == SingletonStore.storeType.IN_DISK){
//            try {
//                dataOut.flush();
//                fileOut.close();
//                dataOut.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        SingletonStatistics.INSTANCE.blockCountPrint();
    }

    /**
     * 将分块元数据信息存起来;用于与影子数据对比，寻找出差异数据
     * @param fileIndex 当前分块的分块号，从0开始
     * @param bytes 当前分块的所有字节集合
     * @param rt 存储策略
     */
    private void blockSave(int fileIndex, byte[] bytes, CallType rt) {
        byte[] md5 = Hashcode.getMD5(bytes);
        int length = bytes.length;
        SingletonNetty.INSTANCE.addBlockInfo(new BlockInfoSrc(md5,fileIndex,length,bytes));
    }
}
