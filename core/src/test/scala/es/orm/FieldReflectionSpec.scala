package es.orm

import org.specs2.mutable.Specification
import es.refine.Refine


class FieldReflectionSpec extends Specification {
  "Field Specification".title


  "Serialization" should {

    "correct manages fk test" in {
//      Refine.fields.foreach(a => println(a.toMap))
      val cb = Refine.getFieldByName("createdBy")

      cb.get.foreignKey must beTrue

      ok
    }

  }
}
