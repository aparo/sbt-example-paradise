import macrocc.Annotations.{Index, Fk}
import macrocc.{Fk2, helloCC}

@helloCC()
case class MyTest(@Index a:Int, @Fk2 b:String="ok")

object MyTest{
  def apply(){
    MyTest(0)
  }
}

@hello
object Test extends App {
  println(this.hello)
  val m = MyTest()
}