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

import simplex3d.math._
import simplex3d.math.float._
import simplex3d.math.float.functions._

import simplex3d.data._
import simplex3d.data.float._

class Tut6Translation extends Tutorial {

  override val name = "Tutorial 6 Translation"

  val cameraToClipMatrix = Mat4(0.0f)
  val seq = DataBuffer[Mat4, RFloat](1)


  def calcFrustumScale(fovDeg: Float) = {
    val fovRad = radians(fovDeg)
    val scale = 1.0f / tan(fovRad / 2.0f)
    scale
  }


  var theProgram = 0
  var cameraToClipMatrixUnif = 0
  var modelToCameraMatrixUnif = 0
  
  val frustumScale = calcFrustumScale(45.0f)

  var vertexBufferObject = 0
  var indexBufferObject = 0

  var vao = 0

  val numberOfVertices = 8

  //HACK: to replace defines
  val GreenColor = Array(0.0f, 1.0f, 0.0f, 1.0f)
  val BlueColor = Array(0.0f, 0.0f, 1.0f, 1.0f)
  val RedColor = Array( 1.0f, 0.0f, 0.0f, 1.0f)
  val GreyColor = Array(0.8f, 0.8f, 0.8f, 1.0f)
  val BrownColor = Array(0.5f, 0.5f, 0.0f, 1.0f)

  val posData = Array(

	+1.0f, +1.0f, +1.0f,
	-1.0f, -1.0f, +1.0f,
	-1.0f, +1.0f, -1.0f,
	+1.0f, -1.0f, -1.0f,

	-1.0f, -1.0f, -1.0f,
	+1.0f, +1.0f, -1.0f,
	+1.0f, -1.0f, +1.0f,
	-1.0f, +1.0f, +1.0f
  )

  val colorData = Array( 
                  GreenColor,
                  BlueColor,
                  RedColor,
                  BrownColor,

                  GreenColor,
                  BlueColor,
                  RedColor,
                  BrownColor
                  ).flatten

  val vertexData = posData ++ colorData

  val indexData = Array[Short](
	0, 1, 2,
	1, 0, 3,
	2, 3, 0,
	3, 2, 1,

	5, 4, 6,
	4, 5, 7,
	7, 6, 4,
	6, 7, 5
  )

  val startTime = System.nanoTime

  val instances = List(new StationaryOffsetInstance(), new OvalOffsetInstance(), new BottomCircleInstance())

  
  def initializeProgram {
    val vertexShader = "data/tut6/PosColorLocalTransform.vert".compile
    val fragmentShader = "data/tut6/ColorPassthrough.frag".compile

    val shaderList = List(vertexShader, fragmentShader)
    theProgram = createProgram(shaderList)

    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix")
    cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix")

    val zNear = 1.0f
    val zFar = 45.0f
    cameraToClipMatrix(0, 0) = frustumScale
    cameraToClipMatrix(1, 1) = frustumScale
    cameraToClipMatrix(2, 2) = (zFar + zNear) / (zNear - zFar)
    cameraToClipMatrix(2, 3) = (2 * zFar * zNear) / (zNear - zFar)
    cameraToClipMatrix(3, 2) = -1.0f

    seq(0) = cameraToClipMatrix

    glUseProgram(theProgram)
    glUniformMatrix4(cameraToClipMatrixUnif, false, seq.buffer)
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

    vao = glGenVertexArrays
    glBindVertexArray(vao)
    
    
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
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    glUseProgram(theProgram)
    
    glBindVertexArray(vao)
    
    val elapsedTime = (System.nanoTime - startTime) / 1000000000.0f
    
    for(instance <- instances) {
      val mat = instance constructMatrix(elapsedTime)
      seq(0) = mat
      glUniformMatrix4(modelToCameraMatrixUnif, false, seq.buffer)
      glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0)
    }
    
    glBindVertexArray(0)
    glUseProgram(0)
  }

  def reshape(width: Int, height: Int) {
    cameraToClipMatrix(0, 0) = frustumScale * (height / width.toFloat)
    cameraToClipMatrix(1, 1) = frustumScale

    glUseProgram(theProgram)
    seq(0) = cameraToClipMatrix
    glUniformMatrix4(cameraToClipMatrixUnif, false, seq.buffer)
    glUseProgram(0)
  }

  def input {
    if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
      finished = true
    }
  }


  abstract class Instance {

    def constructMatrix(elapsedTime: Float) = {
      val mat = Mat4(1.0f)
      val v = Vec4(calcOffset(elapsedTime), 1.0f)
      mat(3) = v
      mat
    }

    def calcOffset(elapsedTime: Float) : Vec3
  }

  class StationaryOffsetInstance extends Instance {
    def calcOffset(elapsedTime: Float) = Vec3(0.0f, 0.0f, -20.0f)
  }

  class OvalOffsetInstance extends Instance {
    val loopDuration = 3.0f

    def calcOffset(elapsedTime: Float) = {
    val scale = 3.14159f * 2.0f / loopDuration;

    val currTimeThroughLoop = elapsedTime % loopDuration
      val offset = Vec3(cos(currTimeThroughLoop * scale) * 4.0f, sin(currTimeThroughLoop * scale) * 6.0f, -20.0f)
      offset
    }
  }

  class BottomCircleInstance extends Instance {
    val loopDuration = 12.0f

    def calcOffset(elapsedTime: Float) = {
    val scale = 3.14159f * 2.0f / loopDuration;

    val currTimeThroughLoop = elapsedTime % loopDuration
      val offset = Vec3(cos(currTimeThroughLoop * scale) * 5.0f, -3.5f, sin(currTimeThroughLoop * scale) * 5.0f -20.0f)
      offset
    }
  }

}

