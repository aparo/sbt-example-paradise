package es

object common{
  def ES_ONLY : Nothing = throw new Exception("This method can only be used in queries translated by ES.")
  def ES_INTERNAL : Nothing = throw new Exception("This method is ES internal and cannot be used alone.")
}
