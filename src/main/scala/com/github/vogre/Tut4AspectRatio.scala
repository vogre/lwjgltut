package com.github.vogre 

import org.lwjgl.opengl.{Display,DisplayMode, GL11, GL15, GL20, GL30, GL32}
import org.lwjgl.input.Keyboard
import org.lwjgl.BufferUtils
import GL11._
import GL15._
import GL20._
import GL30._
import GL32._
import Framework._

class Tut4AspectRatio extends Tutorial {
  
  override val name = "Tutorial 4 Aspect Ratio"

  var theProgram = 0

  var positionBufferObject = 0
  var vao = 0
  var offsetUniform = 0
  var perspectiveMatrixUniform = 0

  val frustumScale = 1.0f
  var perspectiveMatrix = new Array[Float](16)

  val cubeVertices = Array(
	 0.25f,  0.25f, -1.25f, 1.0f,
	 0.25f, -0.25f, -1.25f, 1.0f,
	-0.25f,  0.25f, -1.25f, 1.0f,

	 0.25f, -0.25f, -1.25f, 1.0f,
	-0.25f, -0.25f, -1.25f, 1.0f,
	-0.25f,  0.25f, -1.25f, 1.0f,

	 0.25f,  0.25f, -2.75f, 1.0f,
	-0.25f,  0.25f, -2.75f, 1.0f,
	 0.25f, -0.25f, -2.75f, 1.0f,

	 0.25f, -0.25f, -2.75f, 1.0f,
	-0.25f,  0.25f, -2.75f, 1.0f,
	-0.25f, -0.25f, -2.75f, 1.0f,

	-0.25f,  0.25f, -1.25f, 1.0f,
	-0.25f, -0.25f, -1.25f, 1.0f,
	-0.25f, -0.25f, -2.75f, 1.0f,

	-0.25f,  0.25f, -1.25f, 1.0f,
	-0.25f, -0.25f, -2.75f, 1.0f,
	-0.25f,  0.25f, -2.75f, 1.0f,

	 0.25f,  0.25f, -1.25f, 1.0f,
	 0.25f, -0.25f, -2.75f, 1.0f,
	 0.25f, -0.25f, -1.25f, 1.0f,

	 0.25f,  0.25f, -1.25f, 1.0f,
	 0.25f,  0.25f, -2.75f, 1.0f,
	 0.25f, -0.25f, -2.75f, 1.0f,

	 0.25f,  0.25f, -2.75f, 1.0f,
	 0.25f,  0.25f, -1.25f, 1.0f,
	-0.25f,  0.25f, -1.25f, 1.0f,

	 0.25f,  0.25f, -2.75f, 1.0f,
	-0.25f,  0.25f, -1.25f, 1.0f,
	-0.25f,  0.25f, -2.75f, 1.0f,

	 0.25f, -0.25f, -2.75f, 1.0f,
	-0.25f, -0.25f, -1.25f, 1.0f,
	 0.25f, -0.25f, -1.25f, 1.0f,

	 0.25f, -0.25f, -2.75f, 1.0f,
	-0.25f, -0.25f, -2.75f, 1.0f,
	-0.25f, -0.25f, -1.25f, 1.0f
  )

  val verticesColors = Array(
    0.0f, 0.0f, 1.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f,

    0.0f, 0.0f, 1.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f,

    0.8f, 0.8f, 0.8f, 1.0f,
    0.8f, 0.8f, 0.8f, 1.0f,
    0.8f, 0.8f, 0.8f, 1.0f,

    0.8f, 0.8f, 0.8f, 1.0f,
    0.8f, 0.8f, 0.8f, 1.0f,
    0.8f, 0.8f, 0.8f, 1.0f,

    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,

    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,

    0.5f, 0.5f, 0.0f, 1.0f,
    0.5f, 0.5f, 0.0f, 1.0f,
    0.5f, 0.5f, 0.0f, 1.0f,

    0.5f, 0.5f, 0.0f, 1.0f,
    0.5f, 0.5f, 0.0f, 1.0f,
    0.5f, 0.5f, 0.0f, 1.0f,

    1.0f, 0.0f, 0.0f, 1.0f,
    1.0f, 0.0f, 0.0f, 1.0f,
    1.0f, 0.0f, 0.0f, 1.0f,

    1.0f, 0.0f, 0.0f, 1.0f,
    1.0f, 0.0f, 0.0f, 1.0f,
    1.0f, 0.0f, 0.0f, 1.0f,

    0.0f, 1.0f, 1.0f, 1.0f,
    0.0f, 1.0f, 1.0f, 1.0f,
    0.0f, 1.0f, 1.0f, 1.0f,

    0.0f, 1.0f, 1.0f, 1.0f,
    0.0f, 1.0f, 1.0f, 1.0f,
    0.0f, 1.0f, 1.0f, 1.0f
  )

  val vertexPositions = cubeVertices ++ verticesColors
  
  def initializeProgram {
    val vertexShader = "data/tut4/MatrixPerspective.vert".compile
    val fragmentShader = "data/tut4/StandardColors.frag".compile

    val shaderList = List(vertexShader, fragmentShader)
    theProgram = createProgram(shaderList)

    offsetUniform = glGetUniformLocation(theProgram, "offset")
    perspectiveMatrixUniform = glGetUniformLocation(theProgram, "perspectiveMatrix")

    val zNear = 0.5f
    val zFar = 3.0f
    perspectiveMatrix(0) = frustumScale
    perspectiveMatrix(5) = frustumScale
    perspectiveMatrix(10) = (zFar + zNear) / (zNear - zFar)
    perspectiveMatrix(14) = (2 * zFar * zNear) / (zNear - zFar)
    perspectiveMatrix(11) = -1.0f
    val tmpBuf = BufferUtils.createFloatBuffer(16)
    tmpBuf.put(perspectiveMatrix)
    tmpBuf.flip()

    glUseProgram(theProgram)
    glUniformMatrix4(perspectiveMatrixUniform, false, tmpBuf)
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

    glEnable(GL_CULL_FACE)
    glCullFace(GL_BACK)
    glFrontFace(GL_CW)
  }

  def display {
    
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClear(GL_COLOR_BUFFER_BIT)

    val colorData = cubeVertices.length * 4
    
    glUseProgram(theProgram)
    
    glUniform2f(offsetUniform, 1.5f, 0.5f)

    glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject)
    glEnableVertexAttribArray(0)
    glEnableVertexAttribArray(1)
    glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0)
    glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorData)

    glDrawArrays(GL_TRIANGLES, 0, 36)

    glDisableVertexAttribArray(0)
    glDisableVertexAttribArray(1)
    glUseProgram(0)
  }

  def reshape(width: Int, height: Int) {
    //Here, we change the X scaling based on the ratio of height to width. The Y scaling is left alone.
    perspectiveMatrix(0) = frustumScale * (height / width.toFloat)
    perspectiveMatrix(5) = frustumScale

    val tmpBuf = BufferUtils.createFloatBuffer(16)
    tmpBuf.put(perspectiveMatrix)
    tmpBuf.flip()

    glUseProgram(theProgram)
    glUniformMatrix4(perspectiveMatrixUniform, false, tmpBuf)
    glUseProgram(0)
  }

  def input {
    if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
      finished = true
    }
    if(Keyboard.isKeyDown(Keyboard.KEY_H)) {
      Display.setDisplayMode(new DisplayMode(500, 700))
      reshape(500, 700)
    }
    if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
      Display.setDisplayMode(new DisplayMode(700, 500))
      reshape(700, 500)
    }
  }
}
