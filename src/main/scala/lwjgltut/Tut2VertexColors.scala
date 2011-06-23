package lwjgltut 

import org.lwjgl.opengl.{Display, GL11, GL15, GL20, GL30, GL32}
import org.lwjgl.input.Keyboard
import org.lwjgl.BufferUtils
import GL11._
import GL15._
import GL20._
import GL30._
import GL32._
import Framework._

class Tut2VertexColors extends Tutorial {
  
  override val name = "Tutorial 2 Vertex Colors"
  
  var theProgram = 0

  var positionBufferObject = 0
  var vao = 0

  val vertexPositions = Array(0.0f, 0.5f, 0.0f, 1.0f,
                              0.5f, -0.366f, 0.0f, 1.0f,
                              -0.5f, -0.366f, 0.0f, 1.0f)

  val vertexColors = Array(1.0f, 0.0f, 0.0f, 1.0f,
                              0.0f, 1.0f, 0.0f, 1.0f,
                              0.0f, 0.0f, 1.0f, 1.0f)

  val vertices = vertexPositions ++ vertexColors


  def initializeProgram {
    val vertexShader = "data/tut2/VertexColors.vert".compile
    val fragmentShader = "data/tut2/VertexColors.frag".compile

    val shaderList = List(vertexShader, fragmentShader)
    theProgram = createProgram(shaderList)
  }

  def initializeVertexBuffer {
    positionBufferObject = glGenBuffers()

    var tmpBuffer = BufferUtils.createFloatBuffer(vertices.length)
    tmpBuffer.put(vertices)
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
    glEnableVertexAttribArray(1)
    glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0)
    glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, vertexPositions.length * 4)

    glDrawArrays(GL_TRIANGLES, 0, 3)

    glDisableVertexAttribArray(0)
    glDisableVertexAttribArray(1)
    glUseProgram(0)
  }

  def input {
    if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
      finished = true
    }
  }
}
