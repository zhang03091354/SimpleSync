package com.zcj.nettyServer.tools;

import io.netty.channel.ChannelHandlerContext;
import org.apache.kafka.clients.producer.*;

import java.util.LinkedList;
import java.util.Properties;

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

    //发送的同步消息中，分块信息与分块信息之间的间隔符
    public static final String splitOfBlock = ";,,;";

    //Kafka生产者
    private Producer<String, String> kafkaProducer;
    private final String KAFKA_TOPIC = "sensor";
    public boolean kafkaProducerInit(){
        Properties props = new Properties();
        //kafka 集群，broker-list
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "centos7201:9092");
        props.put("acks", "all");
        //重试次数
        props.put("retries", 1);
        //批次大小
        props.put("batch.size", 16384);
        //等待时间
        props.put("linger.ms", 10);
        //RecordAccumulator 缓冲区大小
        props.put("buffer.memory", 500000000);
        //生产者多能发送的最大数据
        props.put("max.request.size", 524288000);
        // send() 方法调用要么被阻塞，要么抛出异常，取决于如何设置 block.on.buffer.full 参数
        // （在 0.9.0.0 版本里被替换成了 max.block.ms，表示在抛出异常之前可以阻塞一段时间
        props.put("max.block.ms", 20000);
        // 重传等待时间，超过这个时间如果生产者还没有得到回复，则会重发消息，重发次数超了则会报错
        props.put("request.timeout.ms", 30000); //默认也是30s
        //序列化器
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");

        this.kafkaProducer = new KafkaProducer<String, String>(props);
        return true;
    }
    public void kafkaSendMes(String record){
        this.kafkaProducer.send(new ProducerRecord<>(this.KAFKA_TOPIC, record),new Callback() {
                //回调函数，该方法会在 Producer 收到 ack 时调用，为异步调用
                @Override
                public void onCompletion(RecordMetadata metadata,
                                         Exception exception) {
                    if (exception == null) {
                        System.out.println("success->" +
                                metadata.offset());
                    } else {
                        exception.printStackTrace();
                    }
                }
            });
    }
    //netty接收到的消息列表
    public LinkedList<String> nettyMesList = new LinkedList();

    //文件类型
    public static final String TOTALFILE = "total";
    public static final String INDEXFILE = "index";


    //读写文件使用的字符编码
    public static final String One2OneCodeType = "ISO-8859-1";


    public enum ProcessState{
        SERVER_AND_CLIENT_IDLE,

        CLIENT_WAIT_ACK_OF_REQ_CONFIRM,
        CLIENT_PROCESS_SRC_DEBLOCK,
        CLIENT_WAIT_ACK_OF_BACKUP_CHECKSUM,
        CLIENT_SEND_ACK_OF_SRCDIFF_CHECKSUM,
        CLIENT_WAIT_ACK_OF_END,
        CLIENT_UPDATE_CURRENT_STATE,

        SERVER_WAIT_REQ_OF_SYNC,
        SERVER_SEND_ACK_OF_CONFIRM,
        SERVER_PROCESS_BACKUP_DEBLOCK,
        SERVER_WAIT_ACK_OF_SRCDIFF_CHECKSUM,
        SERVER_WAIT_SRCDIFF_CHECKSUM_FINISH,
        SERVER_PROCESS_FILE_GENERATE,
        SERVER_SEND_ACK_OF_END,

        ERROR_STATE
    }

    //发送数据采用同步非阻塞方式,这个参数是当前状态
    private ProcessState clientCurrentState;
    private ProcessState serverCurrentState;

    /**
     * 客户端handler句柄，用于发送数据
     */
    private ChannelHandlerContext clientCtx = null;

    /**
     * 服务器handler句柄，用于发送数据
     */
    private ChannelHandlerContext serverCtx = null;

    //工程内部打印消息的序号
    private static int printId = 0;

    //发送消息的序号
    private static int mesId = 0;

    //当前正在处理的文件
    private SyncFileInfo clientCurrentFile;
    private SyncFileInfo serverCurrentFile;

    //需要处理的文件队列
    private LinkedQueue<SyncFileInfo> fileList = null;

    //客户端收到服务器发送的消息类型队列
    private LinkedQueue<ProcessState> mesListFromServer = (LinkedQueue)new LinkedQueue<>().initQueue();

    //服务器收到客户端发送的消息类型队列
    private LinkedQueue<ProcessState> mesListFromClient = (LinkedQueue)new LinkedQueue<>().initQueue();


    //服务器收到客户端发送的diffChecksum消息队列
    private LinkedQueue<SrcDiffChecksumType> diffChecksumListFromClient = (LinkedQueue)new LinkedQueue<>().initQueue();

    public void printClientMes(String mesType, String mes){
        if (false) return;
        String res = "[Mes "+(this.printId++)+"]-[Client]-["+mesType+"]"+mes;
        System.out.println(res);
    }

    public void printServerMes(String mesType, String mes){
        if (false) return;
        String res = "[Mes "+(this.printId++)+"]--[Server]-["+mesType+"]"+mes;
        System.out.println(res);
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

    public static int getIncrementMesId() {
        int i = mesId;
        mesId++;
        return i;
    }

    public ProcessState getClientCurrentState() {
        return clientCurrentState;
    }

    public synchronized void setClientCurrentState(ProcessState clientCurrentState) {
        this.clientCurrentState = clientCurrentState;
    }

    public SyncFileInfo getClientCurrentFile() {
        return clientCurrentFile;
    }

    public void setClientCurrentFile(SyncFileInfo clientCurrentFile) {
        this.clientCurrentFile = clientCurrentFile;
    }

    public LinkedQueue<ProcessState> getMesListFromServer() {
        return mesListFromServer;
    }

    public ChannelHandlerContext getServerCtx() {
        return serverCtx;
    }

    public void setServerCtx(ChannelHandlerContext serverCtx) {
        this.serverCtx = serverCtx;
    }

    public ProcessState getServerCurrentState() {
        return serverCurrentState;
    }

    public synchronized void setServerCurrentState(ProcessState serverCurrentState) {
        this.serverCurrentState = serverCurrentState;
    }

    public SyncFileInfo getServerCurrentFile() {
        return serverCurrentFile;
    }

    public synchronized void setServerCurrentFile(SyncFileInfo serverCurrentFile) {
        this.serverCurrentFile = serverCurrentFile;
    }

    public LinkedQueue<ProcessState> getMesListFromClient() {
        return mesListFromClient;
    }

    public void setMesListFromClient(LinkedQueue<ProcessState> mesListFromClient) {
        this.mesListFromClient = mesListFromClient;
    }


    public LinkedQueue<SrcDiffChecksumType> getDiffChecksumListFromClient() {
        return diffChecksumListFromClient;
    }

    public void setDiffChecksumListFromClient(LinkedQueue<SrcDiffChecksumType> diffChecksumListFromClient) {
        this.diffChecksumListFromClient = diffChecksumListFromClient;
    }
}
