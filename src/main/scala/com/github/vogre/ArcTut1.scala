package com.github.vogre 

import org.lwjgl.opengl.{Display, GL11, GL15, GL20, GL30, GL32}
import org.lwjgl.input.Keyboard
import org.lwjgl.BufferUtils
import GL11._
import GL15._
import GL20._
import GL30._
import GL32._

class Tut01 extends Tutorial {
  
  override val name = "Tutorial 1"
  
  var theProgram = 0

  var positionBufferObject = 0
  var vao = 0

  val strVertexShader = "#version 330\n" +
                        "layout(location = 0) in vec4 position;\n" +
                        "void main()\n" +
                        "{\n" +
                        "    gl_Position = position;\n" +
                        "}\n";


  val strFragmentShader = "#version 330\n" +
                          "out vec4 outputColor;\n" +
                          "void main()\n" +
                          "{\n" +
                          "    outputColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);\n" +
                          "}\n";
  
  val vertexPositions = Array(0.75f, 0.75f, 0.0f, 1.0f,
                              0.75f, -0.75f, 0.0f, 1.0f,
                              -0.75f, -0.75f, 0.0f, 1.0f)

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

  def createProgram(shaderList: List[Int]) = {
    val program = glCreateProgram
    for(shader <- shaderList) { 
        glAttachShader(program, shader)
    }

    glLinkProgram(program)

    val status = glGetProgram(program, GL_LINK_STATUS)
    if (status == GL_FALSE)
    {
      val infoLogLength = glGetProgram(program, GL_INFO_LOG_LENGTH)
      val infoLog = glGetProgramInfoLog(program, infoLogLength)

      throw new Exception("Link failure: " + infoLog)
    }
    program
  }

  def initializeProgram {
    val vertexShader = createShader(GL_VERTEX_SHADER, strVertexShader)
    val fragmentShader = createShader(GL_FRAGMENT_SHADER, strFragmentShader)

    val shaderList = List(vertexShader, fragmentShader)
    theProgram = createProgram(shaderList)
  }

  def initializeVertexBuffer {
    positionBufferObject = glGenBuffers()

    var tmpBuffer = BufferUtils.createFloatBuffer(vertexPositions.length)
    tmpBuffer.put(vertexPositions)
    tmpBuffer.flip()
    glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject)
    glBufferData(GL_ARRAY_BUFFER, tmpBuffer, GL_STATIC_DRAW)
    glBindBuffer(GL_ARRAY_BUFFER, 0)
  }

  def init {
    initializeProgram
    initializeVertexBuffer

    vao = glGenVertexArrays
    glBindVertexArray(vao)

    glViewport(0, 0, 500, 500)
  }

  def display {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClear(GL_COLOR_BUFFER_BIT)
    
    glUseProgram(theProgram)

    glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject)
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0)

    glDrawArrays(GL_TRIANGLES, 0, 3)

    glDisableVertexAttribArray(0)
    glUseProgram(0)
  }

  def input {
    if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
      finished = true
    }
  }
}
