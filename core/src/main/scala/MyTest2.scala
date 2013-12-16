import macrocc.Annotations.{Fk, Index}
import macrocc.{helloCC}

@helloCC()
case class MyTest2(@Index a:Int, @Fk b:String="ok")

object MyTest2{
  def apply(){
    MyTest2(0)
  }
}
