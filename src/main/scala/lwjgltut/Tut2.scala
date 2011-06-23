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

class Tut02FragPosition extends Tutorial {
  
  override val name = "Tutorial 2 Frag Position"
  
  var theProgram = 0

  var positionBufferObject = 0
  var vao = 0

  val vertexPositions = Array(0.75f, 0.75f, 0.0f, 1.0f,
                              0.75f, -0.75f, 0.0f, 1.0f,
                              -0.75f, -0.75f, 0.0f, 1.0f)



  def initializeProgram {
    val vertexShader = "data/tut2/FragPosition.vert".compile
    val fragmentShader = "data/tut2/FragPosition.frag".compile

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
