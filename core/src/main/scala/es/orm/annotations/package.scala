package es.orm//.annotations

import scala.annotation.{Annotation, StaticAnnotation}
import scala.annotation.meta.field


//class Field extends StaticAnnotation
//
//class Fk extends StaticAnnotation
//
//class Index extends StaticAnnotation
//
//class NoIndex(index: Boolean = true) extends StaticAnnotation
//
//class Nested extends StaticAnnotation
//
//class Stored extends StaticAnnotation
//
//class NoStored extends StaticAnnotation
//
//class Analyzed extends StaticAnnotation
//
//class NotAnalyzed extends StaticAnnotation
//
//class Embedded extends StaticAnnotation

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
