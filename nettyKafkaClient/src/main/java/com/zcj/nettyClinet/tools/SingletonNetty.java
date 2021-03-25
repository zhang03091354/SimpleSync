package com.zcj.nettyClinet.tools;

import io.netty.channel.ChannelHandlerContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public enum SingletonNetty {
    INSTANCE;

    /**
     * 一下参数是LengthFieldBasedFrameDecoder的配置参数
     * |lengthArea|mesArea|
     * 消息中包含长度区域lengthArea，和消息区域mesArea
     * lengthArea的值设置为mesArea的大小，单位为字节
     * 比如 想要发送Hello，大小为5个Byte。那么实际发送的消息为|5|Hello|
     * 这里可以看出一个问题，就是lengthArea的位置和长度如何知道，因此引入两个参数如下：
     * lengthFieldOffset：表示lengthArea从消息的哪个位置开始
     * lengthFieldLength：表示lengthArea的长度大小
     * 另外还有一个参数，就是在解析（decoder环节）时，需要忽略的字节数，参数如下：
     * initialBytesToStrip：如果值为4，表示忽略4个字节，
     * 即decoder解析出来的结果是从|lengthArea|mesArea|的第五个字节开始的，前面4个字节会被忽略
     * 另外还有一个参数，lengthAdjustment，表示mesArea与lengthArea之间有几个字节间隔
     */
    //长度的偏移
    public static final short LENGTH_BYTES_OFFSET = 0;
    //长度的字节数
    public static final short LENGTH_BYTES_LENGTH = 4;
    //解析后忽略的字节数
    public static final short LENGTH_BYTES_IGNORE = 4;
    //lengthAdjustment
    public static final short LENGTH_BYTES_ADJUSTMENT = 0;
    /**
     * LengthFieldBasedFrameDecoder的配置参数 结束
     */

    //读写文件使用的字符编码
    public static final String One2OneCodeType = "ISO-8859-1";

    //文件类型
    public static final String TOTALFILE = "total";
    public static final String INDEXFILE = "index";

    //发送的同步消息中，分块信息与分块信息之间的间隔符
    public static final String splitOfBlock = ";,,;";

    //读取影子数据的线程运行状态，0：正在运行  1：结束
    public static int getShadowDataThreadFlag = 1;

    //影子数据的解析结果
    private HashMap<String, FileLocation> client_MD52Index = new HashMap(4000000);

    //源文件分块结果
    private LinkedQueue<BlockInfoSrc> srcBlockList = new LinkedQueue<>();


    /**
     * 客户端handler句柄，用于发送数据
     */
    private ChannelHandlerContext clientCtx = null;

    //工程内部打印消息的序号
    private static int printId = 0;

    //当前正在处理的文件
    private SyncFileInfo clientCurrentFile;
//
    //需要处理的文件队列
    private LinkedQueue<SyncFileInfo> fileList = null;

    public void printClientMes(String mesType, String mes){
        if(mesType.equals("time")){
            //记录开始时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");//设置日期格式
            String res = "[Mes "+(this.printId++)+"]-[Client]-["+mes+"]时间："+df.format(new Date());
            System.out.println(res);
        }else{
            if (false) return;
            String res = "[Mes "+(this.printId++)+"]-[Client]-["+mesType+"]"+mes;
            System.out.println(res);
        }

    }

    /************** Getter and Setter *********************/
    public LinkedQueue<SyncFileInfo> getFileList() {
        return fileList;
    }

    public void setFileList(LinkedQueue<SyncFileInfo> fileList) {
        this.fileList = fileList;
    }

    public ChannelHandlerContext getClientCtx() {
        return clientCtx;
    }

    public void setClientCtx(ChannelHandlerContext clientCtx) {
        this.clientCtx = clientCtx;
    }

    public HashMap<String, FileLocation> getClient_MD52Index() {
        return client_MD52Index;
    }

    public void setClient_MD52Index(HashMap<String, FileLocation> client_MD52Index) {
        this.client_MD52Index = client_MD52Index;
    }

    public void AddClient_MD52Index(String md5, FileLocation fileLocation) {
        this.client_MD52Index.put(md5,fileLocation);
    }

    public LinkedQueue<BlockInfoSrc> getSrcBlockList() {
        return srcBlockList;
    }

    public void addBlockInfo(BlockInfoSrc bis){
        this.srcBlockList.push(bis);
    }

    public SyncFileInfo getClientCurrentFile() {
        return clientCurrentFile;
    }

    public void setClientCurrentFile(SyncFileInfo clientCurrentFile) {
        this.clientCurrentFile = clientCurrentFile;
    }

}
