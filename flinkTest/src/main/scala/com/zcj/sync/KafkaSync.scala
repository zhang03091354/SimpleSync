package com.zcj.sync


import java.net.URI
import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

object KafkaSync {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "centos7201:9092")
    properties.setProperty("group.id", "consumer-group")
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("auto.offset.reset", "latest")


    val stream3: DataStream[String] = env.addSource(new FlinkKafkaConsumer011[String]("sensor", new SimpleStringSchema(), properties))
    stream3.print()

    var conf = new Configuration
    //conf.set("fs.defaultFS", "hdfs://hadoop102:9000")
    var fs = FileSystem.get(new URI("hdfs://centos7201:9000"),conf,"root")
    fs.mkdirs(new Path("/test/test.txt"))
    fs.close()
    println("over")

    //env.execute("flink-kafka")
  }
}
