package com.github.vogre 
import org.lwjgl.opengl.Display

object Main {

  def main(args: Array[String]) = {
    if(args.length == 0) {
      println("enter tutorial number(1, 2.1, 2.2, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.4)")
      println("(5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4)")
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
        case "4.1" => new Tut4OrthoCube
        case "4.2" => new Tut4ShaderPerspective
        case "4.3" => new Tut4MatrixPerspective
        case "4.4" => new Tut4AspectRatio
        case "5.1" => new Tut5OverlapNoDepth
        case "5.2" => new Tut5BaseVertexOverlap
        case "5.3" => new Tut5DepthBuffer
        case "5.4" => new Tut5VertexClipping
        case "5.5" => new Tut5DepthClamping
        case "6.1" => new Tut6Translation
        case "6.2" => new Tut6Scale
        case "6.3" => new Tut6Rotations
        case "6.4" => new Tut6Hierarchy
      }
      try {
        Framework.play(tut)
      } catch {
        case e: Exception  => System.err.println(e)
      }
    }
  }

}
