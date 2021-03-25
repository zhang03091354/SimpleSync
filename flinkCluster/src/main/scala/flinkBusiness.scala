import java.io.{BufferedOutputStream, FileOutputStream, RandomAccessFile}
import java.net.URI
import java.text.SimpleDateFormat
import java.util.{Date, Properties}

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FileSystem, Path}

import util.control.Breaks._

object flinkBusiness {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //val env = StreamExecutionEnvironment.createRemoteEnvironment()
    val properties = new Properties()
    properties.put("bootstrap.servers", "192.168.1.201:9092")
    properties.put("group.id", "consumer-group")
    properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put("auto.offset.reset", "latest")
    properties.put("fetch.max.bytes", "523288100")
    properties.put("max.partition.fetch.bytes", "523288100")
    val kafkaConsumer011 = new FlinkKafkaConsumer011[String]("sensor", new SimpleStringSchema(), properties)
    println("获得kafka消费者句柄"+kafkaConsumer011)
    val stream3: DataStream[String] = env.addSource(kafkaConsumer011)
    //stream3.print
    val steam2 = stream3.map(handleMes _)

    env.execute("flink-kafka")
  }

  def handleMes(mes: String): Unit = {
    //打印时间
    val date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date)
    println("收到消息:"+date1)
    val blockList = mes.split(";,,;")
    val blockNum = blockList.length //并不是实际的分块数，因为消息格式为 ： fileName分隔符0,index,length分隔符1,bytes分隔符0...
    var isExistFlag: Boolean = true
    if (blockNum < 2) {
      //SingletonNetty.INSTANCE.printServerMes("Error", "同步请求的消息格式错误")
      println("同步请求的消息格式错误")
      return
    }
    val fileBackup = "/test/" + blockList(0) + ".bu"
    val newFile = fileBackup + ".new"

    println(fileBackup+newFile)

    //获取hdfs的文件系统
    val conf = new Configuration
    conf.set("dfs.replication", "1")
    conf.setBoolean("dfs.support.append", true)
    val fs = FileSystem.get(new URI("hdfs://192.168.1.201:9000"), conf, "root")
    println("获得hdfs文件系统")
    //处理过程如下
    //1. 创建新的备份文件 /test/filename.bu.new
    //2. 关闭创建的文件流，防止出现文件争用
    //3. 创建旧备份文件文件流，创建前需要判断是否存在。注意：这里会有一些客户端
    //与服务器不一致的问题，比如客户端有影子数据，而服务器没有备份文件，这种其实
    //是系统异常，这份代码暂时不做处理
    //4. 创建新备份文件append文件流
    //5. 根据消息内容，分块处理
    val fileNewBU = fs.create(new Path(newFile)) //如果文件存在则会清空内容
    fileNewBU.close()
    println("已创新备份文件")
    var fileOldBU: FSDataInputStream = null
    if (fs.exists(new Path(fileBackup))) {
      fileOldBU = fs.open(new Path(fileBackup))
    } else {
      isExistFlag = false
    }
    val fileNewBUAppend = fs.append(new Path(newFile))
    breakable {
      for (i <- 1 until blockNum) {
        //这里之所以是until，是因为只需要取下标 1 到 blockNum-1 的分块
        val str = blockList(i)
        val strList = str.split(",") //按照 ， 分割
        if (strList.length == 0) {
          //SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中单个分块的格式错误")
          println("同步请求中单个分块的格式错误")
          break()
        }
        if (strList(0) == "0") { //相同块，直接从备份文件中读取内容
          if (strList.length != 3) {
            //SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中相同分块的格式错误")
            println("同步请求中相同分块的格式错误")
            break()
          }
          if (!isExistFlag) {
            println("没有备份文件，却需要从原备份文件中读取内容")
            break()
          }
          val index = strList(1).toInt
          val length = strList(2).toInt
          fileOldBU.seek(index.toLong)
          val toWrite = new Array[Byte](length)
//          if (i == 333) {
//            println("333了")
//          }
          val readByte = getByteArray(fileOldBU, toWrite, length) //fileOldBU.read(toWrite,0,length)

          if (readByte != length) {
            //SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中相同分块在读取备份文件时发生读取字节不足的问题！")
            println("同步请求中相同分块在读取备份文件时发生读取字节不足的问题")

            break()
          }
          fileNewBUAppend.write(toWrite) //写如新备份文件
        }
        else if (strList(0) == "1") { //差异块，直接从消息中获取内容
          if (strList.length < 2) {
            //SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中差异分块的格式错误")
            println("同步请求中差异分块的格式错误")
            break()
          }
          val bytesStr = str.substring(2, str.length)
          val toWrite = bytesStr.getBytes("ISO-8859-1")
          fileNewBUAppend.write(toWrite)
        } else {
          //SingletonNetty.INSTANCE.printServerMes("Error", "同步请求中单个分块的格式错误，没有注明是相同块还是差异块！")
          println("同步请求中单个分块的格式错误，没有注明是相同块还是差异块！")
          break()
        }
      }
    }

    println("完成单次同步过程")
    //打印时间
    val date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date)
    println("同步完成:"+date)
    //dataOut.flush()
    if (isExistFlag) {
      fileOldBU.close()
      fs.delete(new Path(fileBackup), true)
    }
    fileNewBUAppend.close()
    fs.rename(new Path(newFile), new Path(fileBackup))
    fs.close()
  }

  def getByteArray(fileOldBU: FSDataInputStream, toWrite: Array[Byte], length: Int): Int = {
    var ret = 0
    while (ret != length) {
      val tem = fileOldBU.read(toWrite, ret, length - ret)
      ret += tem
    }
    ret
  }
}
