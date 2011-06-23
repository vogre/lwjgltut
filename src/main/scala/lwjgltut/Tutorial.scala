package lwjgltut 

trait Tutorial {
  val name: String
  var finished = false

  def init: Unit
  def display: Unit
  def input: Unit
}
