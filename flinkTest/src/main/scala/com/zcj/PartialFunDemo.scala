package com.zcj

object PartialFunDemo {
  def main(args: Array[String]): Unit = {
    def ss:PartialFunction[Any,Int]={
      case i:Int => i+1
    }

    val list = List(1,2,3,4,"abc")
    val list2 = list.collect{case i:Int => i+1}
    println("list2 = "+list2)

  }
}
