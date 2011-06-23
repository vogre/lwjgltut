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

class Tut3MultipleShaders extends Tutorial {
  
  override val name = "Tutorial 3 Multiple Shaders"

  val startTime = System.nanoTime
  
  var theProgram = 0

  var positionBufferObject = 0
  var vao = 0
  var uniformLocation = 0

  val vertexPositions = Array(0.25f, 0.25f, 0.0f, 1.0f,
                              0.25f, -0.25f, 0.0f, 1.0f,
                              -0.25f, -0.25f, 0.0f, 1.0f)



  def initializeProgram {
    val vertexShader = "data/tut3/calcOffset.vert".compile
    val fragmentShader = "data/tut3/calcColor.frag".compile

    val shaderList = List(vertexShader, fragmentShader)
    theProgram = createProgram(shaderList)

    uniformLocation = glGetUniformLocation(theProgram, "time")

    val loopDuration = glGetUniformLocation(theProgram, "loopDuration")
    val fragLoopDuration = glGetUniformLocation(theProgram, "fragLoopDuration")
    glUseProgram(theProgram)
    glUniform1f(loopDuration, 5.0f)
    glUniform1f(fragLoopDuration, 10.0f)
    glUseProgram(0)
  }

  def initializeVertexBuffer {
    positionBufferObject = glGenBuffers()

    val tmpBuffer = BufferUtils.createFloatBuffer(vertexPositions.length)
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
    
    val elapsedTime = (System.nanoTime - startTime) / 1000000000.0f

    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClear(GL_COLOR_BUFFER_BIT)
    
    glUseProgram(theProgram)

    glUniform1f(uniformLocation, elapsedTime)

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
