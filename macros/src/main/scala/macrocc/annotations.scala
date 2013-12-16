package macrocc

import scala.annotation.meta.field
import scala.annotation.{StaticAnnotation, Annotation}

object Annotations {
  type Fk = ORM_Fk @field
  type NORM_ted = ORM_NORM_ted @field
  type Embedded = ORM_Embedded @field
  type Index = ORM_Index @field
  type NoIndex = ORM_NoIndex @field
  type Stored = ORM_Stored @field
  type NoStored = ORM_NoStored @field
  type Analyzed = ORM_Analyzed @field
  type NotAnalyzed = ORM_NotAnalyzed @field

}

class ORM_Field() extends Annotation

class ORM_Fk extends Annotation
class ORM_Index extends Annotation
class ORM_NoIndex(index:Boolean=true) extends Annotation
class ORM_NORM_ted extends Annotation
class ORM_Stored extends Annotation
class ORM_NoStored extends Annotation
class ORM_Analyzed extends Annotation
class ORM_NotAnalyzed extends Annotation
class ORM_Embedded extends Annotation


class Fk2 extends StaticAnnotation
