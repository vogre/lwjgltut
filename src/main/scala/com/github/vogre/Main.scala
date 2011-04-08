package com.github.vogre 

object Main {

  def main(args: Array[String]) = {
    if(args.length == 0) {
      println("enter tutorial number(1, 2.1, 2.2, 3.1)")
    } else {
      val arg = args(0)
      val tut = arg match {
        case "1" => new Tut01
        case "2.1" => new Tut02FragPosition
        case "2.2" => new Tut2VertexColors 
        case "3.1" => new Tut3CpuOffset 
        case "3.2" => new Tut3VertOffset 
        case "3.3" => new Tut3VertCalcOffset 
        case "3.4" => new Tut3MultipleShaders
      }
      try {
        Framework.play(tut)
      } catch {
        case e: Exception  => System.err.println(e)
      }
    }
  }

}
