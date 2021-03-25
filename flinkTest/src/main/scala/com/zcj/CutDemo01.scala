package com.zcj

import scala.tools.nsc.GlobalSymbolLoaders

object CutDemo01 {
  def main(args: Array[String]): Unit = {
    def fun1(s:String, s2:String): Boolean ={
      s.equals(s2)
    }

    implicit class TestEq(s:String){
      def checkEq(ss:String)(f:(String,String)=>Boolean): Boolean ={
        f(s,ss)
      }
    }

    val s1 = "hello"

    val res = s1.checkEq("hello")(_.equals(_))
    println(res)
  }
}
