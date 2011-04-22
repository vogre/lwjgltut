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

class Tut5DepthBuffer extends Tutorial {
  
  override val name = "Tutorial 5 Depth Buffer"

  var theProgram = 0

  var vertexBufferObject = 0
  var indexBufferObject = 0

  var vaoObject1 = 0

  var offsetUniform = 0
  
  var perspectiveMatrixUniform = 0
  val frustumScale = 1.0f
  var perspectiveMatrix = new Array[Float](16)

  
  val numberOfVertices = 36

  val RightExtent = 0.8f
  val LeftExtent = -RightExtent
  val TopExtent = 0.20f
  val MiddleExtent = 0.0f
  val BottomExtent = -TopExtent
  val FrontExtent = -1.25f
  val RearExtent = -1.75f

  //HACK: to replace defines
  val GreenColor = Array(0.75f, 0.75f, 1.0f, 1.0f)
  val BlueColor = Array(0.0f, 0.5f, 0.0f, 1.0f)
  val RedColor = Array( 1.0f, 0.0f, 0.0f, 1.0f)
  val GreyColor = Array(0.8f, 0.8f, 0.8f, 1.0f)
  val BrownColor = Array(0.5f, 0.5f, 0.0f, 1.0f)

  val posData = Array(

	//Object 1 positions
	LeftExtent,	TopExtent,		RearExtent,
	LeftExtent,	MiddleExtent,	FrontExtent,
	RightExtent,	MiddleExtent,	FrontExtent,
	RightExtent,	TopExtent,		RearExtent,

	LeftExtent,	BottomExtent,	RearExtent,
	LeftExtent,	MiddleExtent,	FrontExtent,
	RightExtent,	MiddleExtent,	FrontExtent,
	RightExtent,	BottomExtent,	RearExtent,

	LeftExtent,	TopExtent,		RearExtent,
	LeftExtent,	MiddleExtent,	FrontExtent,
	LeftExtent,	BottomExtent,	RearExtent,

	RightExtent,	TopExtent,		RearExtent,
	RightExtent,	MiddleExtent,	FrontExtent,
	RightExtent,	BottomExtent,	RearExtent,

	LeftExtent,	BottomExtent,	RearExtent,
	LeftExtent,	TopExtent,		RearExtent,
	RightExtent,	TopExtent,		RearExtent,
	RightExtent,	BottomExtent,	RearExtent,

	//Object 2 positions
	TopExtent,		RightExtent,	RearExtent,
	MiddleExtent,	RightExtent,	FrontExtent,
	MiddleExtent,	LeftExtent,	FrontExtent,
	TopExtent,		LeftExtent,	RearExtent,

	BottomExtent,	RightExtent,	RearExtent,
	MiddleExtent,	RightExtent,	FrontExtent,
	MiddleExtent,	LeftExtent,	FrontExtent,
	BottomExtent,	LeftExtent,	RearExtent,

	TopExtent,		RightExtent,	RearExtent,
	MiddleExtent,	RightExtent,	FrontExtent,
	BottomExtent,	RightExtent,	RearExtent,
					
	TopExtent,		LeftExtent,	RearExtent,
	MiddleExtent,	LeftExtent,	FrontExtent,
	BottomExtent,	LeftExtent,	RearExtent,
					
	BottomExtent,	RightExtent,	RearExtent,
	TopExtent,		RightExtent,	RearExtent,
	TopExtent,		LeftExtent,	RearExtent,
	BottomExtent,	LeftExtent,	RearExtent
  )

  val colorData = Array( 
                  GreenColor,
                  GreenColor,
                  GreenColor,
                  GreenColor,

                  BlueColor,
                  BlueColor,
                  BlueColor,
                  BlueColor,

                  RedColor,
                  RedColor,
                  RedColor,

                  GreyColor,
                  GreyColor,
                  GreyColor,
                  
                  BrownColor,
                  BrownColor,
                  BrownColor,
                  BrownColor,
                  
                  // object 2 colors
                  RedColor,
                  RedColor,
                  RedColor,
                  RedColor,

                  BrownColor,
                  BrownColor,
                  BrownColor,
                  BrownColor,

                  BlueColor,
                  BlueColor,
                  BlueColor,

                  GreenColor,
                  GreenColor,
                  GreenColor,

                  GreyColor,
                  GreyColor,
                  GreyColor,
                  GreyColor).flatten

  val vertexData = posData ++ colorData

  val indexData = Array[Short](
	0, 2, 1,
	3, 2, 0,

	4, 5, 6,
	6, 7, 4,

	8, 9, 10,
	11, 13, 12,

	14, 16, 15,
	17, 16, 14
  )

  
  def initializeProgram {
    val vertexShader = "data/tut5/Standard.vert".compile
    val fragmentShader = "data/tut5/Standard.frag".compile

    val shaderList = List(vertexShader, fragmentShader)
    theProgram = createProgram(shaderList)

    offsetUniform = glGetUniformLocation(theProgram, "offset")
    perspectiveMatrixUniform = glGetUniformLocation(theProgram, "perspectiveMatrix")

    val zNear = 1.0f
    val zFar = 3.0f
    perspectiveMatrix(0) = frustumScale
    perspectiveMatrix(5) = frustumScale
    perspectiveMatrix(10) = (zFar + zNear) / (zNear - zFar)
    perspectiveMatrix(14) = (2 * zFar * zNear) / (zNear - zFar)
    perspectiveMatrix(11) = -1.0f
    
    val matrixBuf = array2Buffer(perspectiveMatrix)

    glUseProgram(theProgram)
    glUniformMatrix4(perspectiveMatrixUniform, false, matrixBuf)
    glUseProgram(0)
  }

  def initializeVertexBuffer {
    vertexBufferObject = glGenBuffers()
    val vertexBuffer = array2Buffer(vertexData)

    glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject)
    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
    glBindBuffer(GL_ARRAY_BUFFER, 0)
    
    indexBufferObject = glGenBuffers()

    val indexBuffer = array2Buffer(indexData)
    glBindBuffer(GL_ARRAY_BUFFER, indexBufferObject)
    glBufferData(GL_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)
    glBindBuffer(GL_ARRAY_BUFFER, 0)
  }

  def initializeVertexArrayObjects {

    vaoObject1 = glGenVertexArrays
    glBindVertexArray(vaoObject1)
    
    
    val colorOffset = posData.length * 4

    glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject)
	glEnableVertexAttribArray(0)
	glEnableVertexAttribArray(1)
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
    glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorOffset)

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject)

	glBindVertexArray(0)
  }

  def init {
    initializeProgram
    initializeVertexBuffer
    initializeVertexArrayObjects

    glEnable(GL_CULL_FACE)
    glCullFace(GL_BACK)
    glFrontFace(GL_CW)


	glEnable(GL_DEPTH_TEST)
	glDepthMask(true)
	glDepthFunc(GL_LEQUAL)
	glDepthRange(0.0f, 1.0f)
  }

  def display {
    
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClear(GL_COLOR_BUFFER_BIT)

    glUseProgram(theProgram)
    
    glBindVertexArray(vaoObject1)
	glUniform3f(offsetUniform, 0.0f, 0.0f, 0.0f)
    glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0)
    
	glUniform3f(offsetUniform, 0.0f, 0.0f, -1.0f)
    glDrawElementsBaseVertex(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0, numberOfVertices / 2)
    
    glBindVertexArray(0)
    glUseProgram(0)
  }

  def reshape(width: Int, height: Int) {
    //Here, we change the X scaling based on the ratio of height to width. The Y scaling is left alone.
    perspectiveMatrix(0) = frustumScale * (height / width.toFloat)
    perspectiveMatrix(5) = frustumScale

    val tmpBuf = array2Buffer(perspectiveMatrix)

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
