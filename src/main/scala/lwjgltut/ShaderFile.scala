package lwjgltut

import java.io.{File, FileReader, BufferedReader}
import org.lwjgl.opengl.{GL11, GL15, GL20, GL30, GL32}
import GL11._
import GL15._
import GL20._
import GL30._
import GL32._


class GLSLShaderFile(path: String) {
  
  def createShader(shaderType: Int, shaderSource: String) = {
    val shader = glCreateShader(shaderType)
    glShaderSource(shader, shaderSource)
    

    glCompileShader(shader)

    val status = glGetShader(shader, GL_COMPILE_STATUS)
    if (status == GL_FALSE) {
      val infoLogLength = glGetShader(shader, GL_INFO_LOG_LENGTH)
      val infoLog = glGetShaderInfoLog(shader, infoLogLength)

      val strShaderType = shaderType match {
        case GL_VERTEX_SHADER => "vertex"
        case GL_GEOMETRY_SHADER => "geometry"
        case GL_FRAGMENT_SHADER => "fragment"
      }
      throw new Exception("Compile failure in " + strShaderType + "shader:\n" + infoLog)
    }
    shader
  }

  def getType = {
    val index = path.lastIndexOf('.')
    val ext = path.drop(index + 1)
    ext match {
      case "frag" => GL_FRAGMENT_SHADER
      case "fp" => GL_FRAGMENT_SHADER
      case "vert" => GL_VERTEX_SHADER
      case "vp" => GL_VERTEX_SHADER
    }
  }

  def allLines = {
    val bufReader = new BufferedReader(new FileReader(path))
    val result = new StringBuilder()
    val arr = new Array[Char](1024)
    try {
      var read = 0
      while (read != -1) {
        read = bufReader.read(arr)
        if (read != -1) {
          result.appendAll(arr, 0, read)
        }
      }
    } finally{
      try {
        bufReader.close
      } catch { 
        case e: Exception => 
      }
    }
    result.toString
  }


  def compile = {
    val lines = allLines
    val shaderType = getType
    val shader = createShader(shaderType, lines)
    shader
  }
}
