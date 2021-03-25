package test

object TestScala {
  def main(args: Array[String]): Unit = {
    testNull()
    //test$()
  }

  def test$()={
    val name = "test"
    val sal = 92
    println(s"name=$name, sal=$sal")
  }
  def testNull(){
    val ss : String = null
    val s = null
    println(ss)
    println(s)
  }
}
