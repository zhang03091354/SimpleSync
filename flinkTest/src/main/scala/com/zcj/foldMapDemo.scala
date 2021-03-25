package com.zcj

import scala.collection.mutable

object foldMapDemo {
  def main(args: Array[String]): Unit = {
    val sen = "AAAABBBBCCCC"
    val map = sen.foldLeft(Map[Char, Int]())(charConnt)
    println(map)

    val map2 = mutable.Map[Char,Int]()
    sen.foldLeft(map2)(charCount2)
    println(map2)
  }
  def charConnt(map: Map[Char,Int], char: Char): Map[Char,Int] ={
    map + (char -> (map.getOrElse(char, 0)+1))
  }

  def charCount2(map: mutable.Map[Char,Int],char: Char): mutable.Map[Char,Int] ={
    map += char -> (map.getOrElse(char,0)+1)
  }
}
