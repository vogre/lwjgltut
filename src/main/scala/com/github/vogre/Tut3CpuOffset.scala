package com.github.vogre 

import org.lwjgl.opengl.{Display, GL11, GL15, GL20, GL30, GL32}
import org.lwjgl.input.Keyboard
import org.lwjgl.BufferUtils
import GL11._
import GL15._
import GL20._
import GL30._
import GL32._
import Framework._
import scala.math.{sin, cos, Pi}

class Tut3CpuOffset extends Tutorial {
  
  override val name = "Tutorial 3 CPU Offset"

  val startTime = System.nanoTime
  
  var theProgram = 0

  var positionBufferObject = 0
  var vao = 0

  val vertexPositions = Array(0.25f, 0.25f, 0.0f, 1.0f,
                              0.25f, -0.25f, 0.0f, 1.0f,
                              -0.25f, -0.25f, 0.0f, 1.0f)



  def initializeProgram {
    val vertexShader = "data/tut3/standard.vert".compile
    val fragmentShader = "data/tut3/standard.frag".compile

    val shaderList = List(vertexShader, fragmentShader)
    theProgram = createProgram(shaderList)
  }

  def initializeVertexBuffer {
    positionBufferObject = glGenBuffers()

    val tmpBuffer = BufferUtils.createFloatBuffer(vertexPositions.length)
    tmpBuffer.put(vertexPositions)
    tmpBuffer.flip()
    glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject)
    glBufferData(GL_ARRAY_BUFFER, tmpBuffer, GL_STREAM_DRAW)
    glBindBuffer(GL_ARRAY_BUFFER, 0)
  }

  def init {
    initializeProgram
    initializeVertexBuffer

    vao = glGenVertexArrays
    glBindVertexArray(vao)
  }

  def computePositionOffsets = {
    val loopDuration = 5.0f
    val scale = (Pi * 2.0f / loopDuration).toFloat

    val elapsedTime = (System.nanoTime - startTime) / 1000000000.0f
    val timeThroughLoop = elapsedTime % loopDuration

    val offX = cos(scale * timeThroughLoop).toFloat * 0.5f
    val offY = sin(scale * timeThroughLoop).toFloat * 0.5f
    (offX, offY)
  }

  def adjustVertexData(offsetX: Float, offsetY: Float) {
    val newData = vertexPositions.clone
    var i = 0
    while (i < newData.length) {
      newData(i) += offsetX
      newData(i + 1) += offsetY
      i += 4
    }

    glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject)
    val tmpBuffer = BufferUtils.createFloatBuffer(newData.length)
    tmpBuffer.put(newData)
    tmpBuffer.flip()
    glBufferSubData(GL_ARRAY_BUFFER, 0, tmpBuffer)
    glBindBuffer(GL_ARRAY_BUFFER, 0)

  }

  def display {
    
    val (offX, offY) = computePositionOffsets
    adjustVertexData(offX, offY)


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
