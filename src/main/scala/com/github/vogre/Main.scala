package com.github.vogre 

import java.io.File

object Main {

  def main(args: Array[String]) = {
    if(args.length == 0) {
      println("enter tutorial number(1, 2.1, 2.2)")
    } else {
      val arg = args(0)
      val tut = arg match {
        case "1" => new Tut01
        case "2.1" => new Tut02FragPosition
        case "2.2" => new Tut2VertexColors 
      }
      try {
        Framework.play(tut)
      } catch {
        case e: Exception  => System.err.println(e)
      }
    }
  }

}
