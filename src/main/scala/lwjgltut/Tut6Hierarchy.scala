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

import scala.collection.mutable.Stack

class Tut6Hierarchy extends Tutorial {

  override val name = "Tutorial 6 Rotations"

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
  var positionAttrib = 0
  var colorAttrib = 0
  
  val frustumScale = calcFrustumScale(45.0f)

  var vertexBufferObject = 0
  var indexBufferObject = 0

  var vao = 0

  val numberOfVertices = 24

  //HACK: to replace defines
  val GreenColor = Array(0.0f, 1.0f, 0.0f, 1.0f)
  val BlueColor = Array(0.0f, 0.0f, 1.0f, 1.0f)
  val RedColor = Array( 1.0f, 0.0f, 0.0f, 1.0f)
  val YellowColor = Array(1.0f, 1.0f, 0.0f, 1.0f)
  val CyanColor = Array(0.0f, 1.0f, 1.0f, 1.0f)
  val MagentaColor = Array( 1.0f, 0.0f, 1.0f, 1.0f)

  val posData = Array(

    //Front
    +1.0f, +1.0f, +1.0f,
    +1.0f, -1.0f, +1.0f,
    -1.0f, -1.0f, +1.0f,
    -1.0f, +1.0f, +1.0f,

    //Top
    +1.0f, +1.0f, +1.0f,
    -1.0f, +1.0f, +1.0f,
    -1.0f, +1.0f, -1.0f,
    +1.0f, +1.0f, -1.0f,

    //Left
    +1.0f, +1.0f, +1.0f,
    +1.0f, +1.0f, -1.0f,
    +1.0f, -1.0f, -1.0f,
    +1.0f, -1.0f, +1.0f,

    //Back
    +1.0f, +1.0f, -1.0f,
    -1.0f, +1.0f, -1.0f,
    -1.0f, -1.0f, -1.0f,
    +1.0f, -1.0f, -1.0f,

    //Bottom
    +1.0f, -1.0f, +1.0f,
    +1.0f, -1.0f, -1.0f,
    -1.0f, -1.0f, -1.0f,
    -1.0f, -1.0f, +1.0f,

    //Right
    -1.0f, +1.0f, +1.0f,
    -1.0f, -1.0f, +1.0f,
    -1.0f, -1.0f, -1.0f,
    -1.0f, +1.0f, -1.0f
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
                  RedColor,
                  
                  YellowColor,
                  YellowColor,
                  YellowColor,
                  YellowColor,
                  
                  CyanColor,
                  CyanColor,
                  CyanColor,
                  CyanColor,

                  MagentaColor,
                  MagentaColor,
                  MagentaColor,
                  MagentaColor
                  ).flatten

  val vertexData = posData ++ colorData

  val indexData = Array[Short](
    0, 1, 2,
    2, 3, 0,

    4, 5, 6,
    6, 7, 4,

    8, 9, 10,
    10, 11, 8,

    12, 13, 14,
    14, 15, 12,

    16, 17, 18,
    18, 19, 16,

    20, 21, 22,
    22, 23, 20
  )

  val startTime = System.nanoTime

  val armature = new Hierarchy()
  
  def initializeProgram {
    val vertexShader = "data/tut6/PosColorLocalTransform.vert".compile
    val fragmentShader = "data/tut6/ColorPassthrough.frag".compile

    val shaderList = List(vertexShader, fragmentShader)
    theProgram = createProgram(shaderList)

    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix")
    cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix")

    positionAttrib = glGetAttribLocation(theProgram, "position")
    colorAttrib = glGetAttribLocation(theProgram, "color")

    val zNear = 1.0f
    val zFar = 100.0f
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

  def initializeVAO {
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

    vao = glGenVertexArrays
    glBindVertexArray(vao)
    
    val colorOffset = posData.length * 4

    glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject)
    glEnableVertexAttribArray(positionAttrib)
    glEnableVertexAttribArray(colorAttrib)
    glVertexAttribPointer(positionAttrib, 3, GL_FLOAT, false, 0, 0)
    glVertexAttribPointer(colorAttrib, 4, GL_FLOAT, false, 0, colorOffset)

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject)
    glBindVertexArray(0)
  }


  def init {
    initializeProgram
    initializeVAO

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
    glClearDepth(1.0f)
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    armature.draw(this)
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
    while (Keyboard.next()) {
      if(Keyboard.getEventKeyState) {
        val key = Keyboard.getEventCharacter()
        key match {
          case 'a' => armature.adjBase(true)
          case 'd' => armature.adjBase(false)
          case 'w' => armature.adjUpperArm(false)
          case 's' => armature.adjUpperArm(true)
          case 'r' => armature.adjLowerArm(false)
          case 'f' => armature.adjLowerArm(true)
          case 't' => armature.adjWristPitch(false)
          case 'g' => armature.adjWristPitch(true)
          case 'z' => armature.adjWristRoll(false)
          case 'c' => armature.adjWristRoll(true)
          case 'q' => armature.adjFingerOpen(false)
          case 'e' => armature.adjFingerOpen(true)
          case _ => 
        }
      }
    }
  }
}


class MatrixStack {
  var currentMatrix = Mat4(1.0f)

  var matrixStack = new Stack[Mat4]()
  
  def top = currentMatrix

  def rotateX(angDeg: Float) {
    val angRad = radians(angDeg)
    val rcos = cos(angRad)
    val rsin = sin(angRad)
    val mat = Mat3(1.0f)
    mat(1, 1) = rcos
    mat(1, 2) = -rsin
    mat(2, 1) = rsin
    mat(2, 1) = rcos
    currentMatrix *= Mat4(mat)
  }
  
  def rotateY(angDeg: Float) {
    val angRad = radians(angDeg)
    val rcos = cos(angRad)
    val rsin = sin(angRad)
    val mat = Mat3(1.0f)
    mat(0, 0) = rcos
    mat(0, 2) = rsin
    mat(2, 0) = -rsin
    mat(2, 2) = rcos
    currentMatrix *= Mat4(mat)
  }

  def rotateZ(angDeg: Float) {
    val angRad = radians(angDeg)
    val rcos = cos(angRad)
    val rsin = sin(angRad)
    val mat = Mat3(1.0f)
    mat(0, 0) = rcos
    mat(0, 1) = -rsin
    mat(1, 0) = rsin
    mat(1, 1) = rcos
    currentMatrix *= Mat4(mat)
  }

  def scale(scaleVec: Vec3) {
    val scaleMat = Mat4(1.0f)
    scaleMat(0, 0) = scaleVec.x
    scaleMat(1, 1) = scaleVec.y
    scaleMat(2, 2) = scaleVec.z
    currentMatrix *= scaleMat
  }

  def translate(offsetVec: Vec3) {
    val translateMat = Mat4(1.0f)
    translateMat(3) = Vec4(offsetVec, 1.0f)
    currentMatrix *= translateMat
  }

  def push {
    matrixStack.push(currentMatrix.clone)
  }

  def pop {
    currentMatrix = matrixStack.top
    matrixStack.pop
  }

}

class Hierarchy() {
  val StandardAngleIncrement = 11.25f
  val SmallAngleIncrement = 11.25f

  var posBase = Vec3(3.0f, -5.0f, -40.0f)
  var angBase = -45.0f
  var posBaseLeft = Vec3(2.0f, 0.0f, 0.0f)
  var posBaseRight = Vec3(-2.0f, 0.0f, 0.0f)
  var scaleBaseZ = 3.0f
  var angUpperArm = -33.75f
  var sizeUpperArm = 9.0f
  var posLowerArm = Vec3(0.0f, 0.0f, 8.0f)
  var angLowerArm = 146.25f
  var lenLowerArm = 5.0f
  var widthLowerArm = 1.5f
  var posWrist = Vec3(0.0f, 0.0f, 5.0f)
  var angWristRoll = 0.0f
  var angWristPitch = 67.5f
  var lenWrist = 2.0f
  var widthWrist = 2.0f
  var posLeftFinger = Vec3(1.0f, 0.0f, 1.0f)
  var posRightFinger = Vec3(-1.0f, 0.0f, 1.0f)
  var angFingerOpen = 180.0f
  var lenFinger = 2.0f
  var widthFinger = 0.5f
  var angLowerFinger = 45.0f

  val seq = DataBuffer[Mat4, RFloat](1)

  def draw(tutorial: Tut6Hierarchy) {
    var modelToCameraStack = new MatrixStack()
    modelToCameraStack.translate(posBase)
    modelToCameraStack.rotateY(angBase)

    glUseProgram(tutorial.theProgram)
    glBindVertexArray(tutorial.vao)

    modelToCameraStack.push
    modelToCameraStack.translate(posBaseLeft)
    modelToCameraStack.scale(Vec3(1.0f, 1.0f, scaleBaseZ))
    seq(0) = modelToCameraStack.top
    glUniformMatrix4(tutorial.modelToCameraMatrixUnif, false, seq.buffer)
    glDrawElements(GL_TRIANGLES, tutorial.indexData.length, GL_UNSIGNED_SHORT, 0)
    modelToCameraStack.pop

    modelToCameraStack.push
    modelToCameraStack.translate(posBaseRight)
    modelToCameraStack.scale(Vec3(1.0f, 1.0f, scaleBaseZ))
    seq(0) = modelToCameraStack.top
    glUniformMatrix4(tutorial.modelToCameraMatrixUnif, false, seq.buffer)
    glDrawElements(GL_TRIANGLES, tutorial.indexData.length, GL_UNSIGNED_SHORT, 0)
    modelToCameraStack.pop

    drawUpperArm(tutorial, modelToCameraStack)
  }

  def drawUpperArm(tutorial: Tut6Hierarchy, modelToCameraStack: MatrixStack) {
    modelToCameraStack.push
    modelToCameraStack.rotateX(angUpperArm)

    modelToCameraStack.push
    modelToCameraStack.translate(Vec3(0.0f, 0.0f, (sizeUpperArm / 2.0f) - 1.0f))
    modelToCameraStack.scale(Vec3(1.0f, 1.0f, (sizeUpperArm / 2.0f)))
    seq(0) = modelToCameraStack.top
    glUniformMatrix4(tutorial.modelToCameraMatrixUnif, false, seq.buffer)
    glDrawElements(GL_TRIANGLES, tutorial.indexData.length, GL_UNSIGNED_SHORT, 0)
    modelToCameraStack.pop
    
    drawLowerArm(tutorial, modelToCameraStack)
    modelToCameraStack.pop
  }
  
  def drawLowerArm(tutorial: Tut6Hierarchy, modelToCameraStack: MatrixStack) {
    modelToCameraStack.push
    modelToCameraStack.translate(posLowerArm)
    modelToCameraStack.rotateX(angLowerArm)

    modelToCameraStack.push
    modelToCameraStack.translate(Vec3(0.0f, 0.0f, lenLowerArm / 2.0f))
    modelToCameraStack.scale(Vec3(widthLowerArm / 2.0f, widthLowerArm / 2.0f, lenLowerArm / 2.0f))
    seq(0) = modelToCameraStack.top
    glUniformMatrix4(tutorial.modelToCameraMatrixUnif, false, seq.buffer)
    glDrawElements(GL_TRIANGLES, tutorial.indexData.length, GL_UNSIGNED_SHORT, 0)
    modelToCameraStack.pop

    drawWrist(tutorial, modelToCameraStack)

    modelToCameraStack.pop
  }
  
  def drawWrist(tutorial: Tut6Hierarchy, modelToCameraStack: MatrixStack) {
    modelToCameraStack.push
    modelToCameraStack.translate(posWrist)
    modelToCameraStack.rotateZ(angWristRoll)
    modelToCameraStack.rotateX(angWristPitch)

    modelToCameraStack.push
    modelToCameraStack.scale(Vec3(widthWrist / 2.0f, widthWrist / 2.0f, lenWrist / 2.0f))
    seq(0) = modelToCameraStack.top
    glUniformMatrix4(tutorial.modelToCameraMatrixUnif, false, seq.buffer)
    glDrawElements(GL_TRIANGLES, tutorial.indexData.length, GL_UNSIGNED_SHORT, 0)
    modelToCameraStack.pop

    drawFingers(tutorial, modelToCameraStack)

    modelToCameraStack.pop
  }
  
  def drawFingers(tutorial: Tut6Hierarchy, modelToCameraStack: MatrixStack) {
    modelToCameraStack.push
    modelToCameraStack.translate(posLeftFinger)
    modelToCameraStack.rotateY(angFingerOpen)

    //left finger
    modelToCameraStack.push
    modelToCameraStack.translate(Vec3(0.0f, 0.0f, lenFinger / 2.0f))
    modelToCameraStack.scale(Vec3(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f))
    seq(0) = modelToCameraStack.top
    glUniformMatrix4(tutorial.modelToCameraMatrixUnif, false, seq.buffer)
    glDrawElements(GL_TRIANGLES, tutorial.indexData.length, GL_UNSIGNED_SHORT, 0)
    modelToCameraStack.pop

    //lower left finger
    modelToCameraStack.push
    modelToCameraStack.translate(Vec3(0.0f, 0.0f, lenFinger))
    modelToCameraStack.rotateY(-angLowerFinger)
    
    modelToCameraStack.push
    modelToCameraStack.translate(Vec3(0.0f, 0.0f, lenFinger / 2.0f))
    modelToCameraStack.scale(Vec3(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f))
    seq(0) = modelToCameraStack.top
    glUniformMatrix4(tutorial.modelToCameraMatrixUnif, false, seq.buffer)
    glDrawElements(GL_TRIANGLES, tutorial.indexData.length, GL_UNSIGNED_SHORT, 0)
    modelToCameraStack.pop

    modelToCameraStack.pop

    modelToCameraStack.pop


    modelToCameraStack.push
    modelToCameraStack.translate(posRightFinger)
    modelToCameraStack.rotateY(-angFingerOpen)

    //left finger
    modelToCameraStack.push
    modelToCameraStack.translate(Vec3(0.0f, 0.0f, lenFinger / 2.0f))
    modelToCameraStack.scale(Vec3(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f))
    seq(0) = modelToCameraStack.top
    glUniformMatrix4(tutorial.modelToCameraMatrixUnif, false, seq.buffer)
    glDrawElements(GL_TRIANGLES, tutorial.indexData.length, GL_UNSIGNED_SHORT, 0)
    modelToCameraStack.pop

    //lower left finger
    modelToCameraStack.push
    modelToCameraStack.translate(Vec3(0.0f, 0.0f, lenFinger))
    modelToCameraStack.rotateY(angLowerFinger)
    
    modelToCameraStack.push
    modelToCameraStack.translate(Vec3(0.0f, 0.0f, lenFinger / 2.0f))
    modelToCameraStack.scale(Vec3(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f))
    seq(0) = modelToCameraStack.top
    glUniformMatrix4(tutorial.modelToCameraMatrixUnif, false, seq.buffer)
    glDrawElements(GL_TRIANGLES, tutorial.indexData.length, GL_UNSIGNED_SHORT, 0)
    modelToCameraStack.pop

    modelToCameraStack.pop

    modelToCameraStack.pop
  }

  def incrementAngle(angle: Float, inc: Boolean) = if (inc) angle + 11.25f else angle - 11.25f
  def incrementAngleSmall(angle: Float, inc: Boolean) = if (inc) angle + 9.0f else angle - 9.0f

  def adjBase(increment: Boolean) {
    angBase = incrementAngle(angBase, increment)
    angBase = angBase % 360.0f
  }

  def adjUpperArm(increment: Boolean) {
    angUpperArm = incrementAngle(angUpperArm, increment)
    angUpperArm = clamp(angUpperArm, -90.0f, 0.0f)
  }

  def adjLowerArm(increment: Boolean) { 
    angLowerArm = incrementAngle(angLowerArm, increment)
    angLowerArm = clamp(angLowerArm, 0.0f, 146.25f)
  }

  def adjWristPitch(increment: Boolean) { 
    angWristPitch = incrementAngle(angWristPitch, increment)
    angWristPitch = clamp(angWristPitch, 0.0f, 90.0f)
  }
  
  def adjWristRoll(increment: Boolean) { 
    angWristRoll = incrementAngle(angWristRoll, increment)
    angWristRoll = angWristRoll % 360.0f
  }

  def adjFingerOpen(increment: Boolean) {
    angFingerOpen = incrementAngleSmall(angFingerOpen, increment)
    angFingerOpen = clamp(angFingerOpen, 9.0f, 180.0f)
  }
}
