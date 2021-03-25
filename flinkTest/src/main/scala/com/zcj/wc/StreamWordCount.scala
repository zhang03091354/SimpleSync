package com.zcj.wc

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object StreamWordCount {

  def main(args: Array[String]): Unit = {
    //从 外 部 命 令 中 获 取 参 数
//    val params: ParameterTool = ParameterTool.fromArgs(args)
//    val host: String = params.get("host")
//    val port: Int = params.getInt("port")
    //创 建 流 处 理 环 境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //接 收 socket文 本 流
    //val textDstream: DataStream[String] = env.socketTextStream(host, port)
    val textDstream: DataStream[String] = env.socketTextStream("centos7201", 7777)
    // flatMap和 Map 需 要 引 用 的 隐 式 转 换

    val dataStream: DataStream[(String, Int)] =
      textDstream.flatMap(_.split(" ")).filter(_.nonEmpty).map((_, 1)).keyBy(0).sum(1)

    dataStream.print().setParallelism(1)
    //启 动 executor， 执 行 任 务
    env.execute("Socket stream word count")
  }


}
