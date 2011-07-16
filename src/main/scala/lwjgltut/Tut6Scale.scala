package lwjgltut

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

class Tut6Scale extends Tutorial {

  override val name = "Tutorial 6 Scale"

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

  val instances = List(
      new Instance(Vec3(0.0f, 0.0f, -45.0f), nullScale),
      new Instance(Vec3(-10.0f, -10.0f, -45.0f), staticUniformScale),
      new Instance(Vec3(-10.0f, 10.0f, -45.0f), staticNonUniformScale),
      new Instance(Vec3(10.0f, 10.0f, -45.0f), dynamicUniformScale),
      new Instance(Vec3(10.0f, -10.0f, -45.0f), dynamicNonUniformScale)
  )

  
  def initializeProgram {
    val vertexShader = "data/tut6/PosColorLocalTransform.vert".compile
    val fragmentShader = "data/tut6/ColorPassthrough.frag".compile

    val shaderList = List(vertexShader, fragmentShader)
    theProgram = createProgram(shaderList)

    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix")
    cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix")

    val zNear = 1.0f
    val zFar = 61.0f
    cameraToClipMatrix.m00 = frustumScale
    cameraToClipMatrix.m11 = frustumScale
    cameraToClipMatrix.m22 = (zFar + zNear) / (zNear - zFar)
    cameraToClipMatrix.m23 = (2 * zFar * zNear) / (zNear - zFar)
    cameraToClipMatrix.m32 = -1.0f

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
    cameraToClipMatrix.m00 = frustumScale * (height / width.toFloat)
    cameraToClipMatrix.m11 = frustumScale

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

  class Instance(offset: Vec3, scaleCalc: Float => Vec3) {

    def constructMatrix(elapsedTime: Float) = {
      val mat = Mat4(1.0f)
      val v = scaleCalc(elapsedTime)
      mat.m00 = v.x
      mat.m11 = v.y
      mat.m22 = v.z
      mat(3) = Vec4(offset, 1.0f)
      mat
    }

  }

  def calcLerpFactor(elapsedTime: Float, loopDuration: Float) = {
    var value = (elapsedTime % loopDuration) / loopDuration
    if (value > 0.5f)
        value = 1 - value
    2.0f * value
  }
  
  def nullScale(elapsedTime: Float) = Vec3(1.0f, 1.0f, 1.0f)

  def staticUniformScale(elapsedTime: Float) = Vec3(4.0f, 4.0f, 4.0f)

  def staticNonUniformScale(elapsedTime: Float) = Vec3(0.5f, 1.0f, 10.0f)
  
  def dynamicUniformScale(elapsedTime: Float) = {
    val loopDuration = 3.0f
    val scale = Vec3(mix(1.0f, 4.0f, calcLerpFactor(elapsedTime, loopDuration)))
    scale
  }
  
  def dynamicNonUniformScale(elapsedTime: Float) = {
    val xLoopDuration = 3.0f
    val zLoopDuration = 3.0f
    val scale = Vec3(mix(1.0f, 4.0f, calcLerpFactor(elapsedTime, xLoopDuration)),
                    1.0f,
                    mix(1.0f, 10.0f, calcLerpFactor(elapsedTime, zLoopDuration)))
    scale
  }
}

