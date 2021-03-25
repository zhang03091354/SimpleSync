package test

import java.io.{File, FileOutputStream}
import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.IOUtils

import scala.collection.mutable.ArrayBuffer

object testHdfs {
  def main(args: Array[String]): Unit = {
    val conf = new Configuration
    conf.set("dfs.replication", "1")
    conf.setBoolean("dfs.support.append", true)
    //conf.set("fs.defaultFS", "hdfs://hadoop102:9000")
    val fs = FileSystem.get(new URI("hdfs://centos7201:9000"), conf, "root")

    //1. 创建新目录 创建文件用 fs.create(new Path("/test/1.txt"))
    //fs.mkdirs(new Path("/test"))
    //2. 上传本地文件到hdfs
    //fs.copyFromLocalFile(new Path("e:/1.pptx"), new Path("/test/1.pptx"))
    //3. 文件下载
    //fs.copyToLocalFile(false, new Path("/test/1.pptx"), new Path("e:/1_1.pptx"), true)
    //4. 文件夹、文件删除
    //fs.delete(new Path("/test/test.txt"), true)
    //5. 文件名更改
    //fs.rename(new Path("/test/1.pptx"), new Path("/test/2.pptx"))
    //6. 文件详情查看
    //    val listFiles = fs.listFiles(new Path("/"), true)
    //    while (listFiles.hasNext) {
    //      val status = listFiles.next
    //      // 输出详情
    //      // 文件名称
    //      System.out.println(status.getPath.getName)
    //      // 长度
    //      System.out.println(status.getLen)
    //      // 权限
    //      System.out.println(status.getPermission)
    //      // 分组
    //      System.out.println(status.getGroup)
    //      // 获取存储的块信息
    //      val blockLocations = status.getBlockLocations
    //      for (blockLocation <- blockLocations) { // 获取块存储的主机节点
    //        val hosts = blockLocation.getHosts
    //        for (host <- hosts) {
    //          System.out.println(host)
    //        }
    //      }
    //      System.out.println("-----------分割线----------")
    //    }
    //7. 文件和文件夹判断
    //    val listStatus = fs.listStatus(new Path("/"))
    //    for (fileStatus <- listStatus) { // 如果是文件
    //      if (fileStatus.isFile) {
    //        System.out.println("f:" + fileStatus.getPath.getName)
    //      }
    //      else {
    //        System.out.println("d:" + fileStatus.getPath.getName)
    //      }
    //    }
    //8. 流对拷 本地-》hdfs
    //9. 流对拷 hdfs-》本地

    //10. 追加内容
    //11. 定位文件读取

    //fs.create(new Path("/test/test2.pptx"))
    //fs.create(new Path("/test/test3.txt"))

    //    val fis = fs.open(new Path("/test/2.pptx"))
    //    val fos = new FileOutputStream(new File("e:/tt.pptx"))
    //    val out = fs.append(new Path("/test/test2.pptx"))
    //    val toWrite = new Array[Byte](100)
    //    for (i <- 0 until 100){
    //      toWrite(i) = i.toByte
    //    }
    //    IOUtils.copyBytes(fis,out,new Configuration())

    fs.delete(new Path("/test/*"), true)

    //fs.delete(new Path("/test/1.pptx.bu"), true)
//    fs.delete(new Path("/test/2.pptx"), true)
//    fs.delete(new Path("/test/test2.pptx"), true)
//    fs.delete(new Path("/test/test3.txt"), true)
    //    val out2 = fs.append(new Path("/test/test3.txt"))
    //    out2.write(toWrite)
    //IOUtils.copyBytes(toWrite,out,100,true)

    //val toWrite = "ss hha ".getBytes("utf-8")

//    val out3 = fs.create(new Path("/test/test.pptx"))
//    out3.close()
    //val out4 = fs.append(new Path("/test/test.pptx"))
    //out4.write(toWrite)

    //fs.open(new Path("/test/tesst.pptx"))



    fs.close()
    println("over")
  }
}
