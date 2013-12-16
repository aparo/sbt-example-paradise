import macrocc.Annotations.{NoIndex, Fk}
import macrocc.helloCC

@helloCC()
case class MyTest(@NoIndex a:Int, @Fk b:String="ok")

object MyTest{
  def apply():MyTest=MyTest(0)

}

@hello
object Test extends App {
  println(this.hello)
  val m = MyTest()
  println(m.a)
}