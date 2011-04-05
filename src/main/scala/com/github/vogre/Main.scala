package com.github.vogre 

object Main {

  def main(args: Array[String]) = {
    val tut01 = new Tut01
    try {
    Framework.play(tut01)
    } catch {
      case e: Exception  => System.err.println(e)
    }
  }

}
