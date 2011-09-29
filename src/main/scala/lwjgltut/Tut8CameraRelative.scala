package lwjgltut.tutorial8

import org.lwjgl.opengl.{Display, GL11, GL15, GL20, GL30, GL31, GL32}
import org.lwjgl.input.Keyboard
import org.lwjgl.BufferUtils
import GL11._
import GL15._
import GL20._
import GL30._
import GL31._
import GL32._
import lwjgltut.framework._
import lwjgltut.Framework._
import lwjgltut.Tutorial

import simplex3d.math._
import simplex3d.math.float._
import simplex3d.math.float.functions._

import simplex3d.data._
import simplex3d.data.float._

class Tutorial8CameraRelative extends Tutorial {

  override val name = "Camera Relative"

  var program = 0
  
  var modelToCameraUnif = 0
  var cameraToClipUnif = 0
  var baseColorUnif = 0

  var ship : Mesh = _
  var plane : Mesh = _

  var orientation = Quat4.Identity.mutableCopy

  var offsetRelative = 0

  val modelRelative = 0
  val worldRelative = 1
  val cameraRelative = 2
  val numRelatives = 3


  var sphereCamRelPos = Vec3(90.0f, 0.0f, 66.0f)
  var camTarget = Vec3(0.0f, 10.0f, 0.0f)

  val seq = DataBuffer[Mat4, RFloat](1)

  var cameraToClipMat = Mat4(1.0f)

  def init {
    val vert = "data/tut8/PosColorLocalTransform.vert".compile
    val frag = "data/tut8/ColorMultUniform.frag".compile
    program = createProgram(List(vert, frag))

    modelToCameraUnif = glGetUniformLocation(program, "modelToCameraMatrix")
    cameraToClipUnif = glGetUniformLocation(program, "cameraToClipMatrix")
    baseColorUnif = glGetUniformLocation(program, "baseColor")

    ship = Mesh.load("data/tut8/Ship.xml")
    plane = Mesh.load("data/tut8/UnitPlane.xml")

    glEnable(GL_CULL_FACE)
    glCullFace(GL_BACK)
    glFrontFace(GL_CW)


    glEnable(GL_DEPTH_TEST)
    glDepthMask(true)
    glDepthFunc(GL_LEQUAL)
    glDepthRange(0.0f, 1.0f)
    glEnable(GL_DEPTH_CLAMP)

    reshape(500, 500)
  }


  def resolveCamPositon = {
    val tempMat = new MatrixStack
    val phi = radians(sphereCamRelPos.x)
    val theta = radians(sphereCamRelPos.y + 90.0f)

    val sinTheta = sin(theta)
    val cosTheta = cos(theta)
    val sinPhi = sin(phi)
    val cosPhi = cos(phi)

    val dirToCamera = Vec3(sinTheta * cosPhi, cosTheta, sinTheta * sinPhi)
    val result = dirToCamera * sphereCamRelPos.z + camTarget
    result
  }

  def calcLookAtMatrix(cameraPt: Vec3, lookPt: inVec3, upPt: inVec3) = {
    val lookDir = normalize(lookPt - cameraPt)
    val upDir = normalize(upPt)
    val rightDir = normalize(cross(lookDir, upDir))
    val perpUpDir = cross(rightDir, lookDir)

    val rotMat = Mat4(1.0f)
    rotMat(0) = Vec4(rightDir, 0.0f)
    rotMat(1) = Vec4(perpUpDir, 0.0f)
    rotMat(2) = Vec4(-lookDir, 0.0f)

    val transposed = transpose(rotMat)

    val transMat = Mat4(1.0f)
    transMat(3) = Vec4(-cameraPt, 1.0f)

    val result = transposed * transMat
    result
  }
  

  def display {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClearDepth(1.0f)
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    val currentMatrix = new MatrixStack
    val camPos = resolveCamPositon
    currentMatrix.setMatrix(calcLookAtMatrix(camPos, camTarget, Vec3.UnitY))


    glUseProgram(program)

    currentMatrix.withMatrix {
      currentMatrix.scale(Vec3(100.0f, 1.0f, 100.0f))
      glUniform4f(baseColorUnif, 0.2f, 0.5f, 0.2f, 1.0f)
      seq(0) = currentMatrix.current
      glUniformMatrix4(modelToCameraUnif, false, seq.buffer)
      plane.render
    }

    currentMatrix.withMatrix {
      currentMatrix.translate(camTarget)
      currentMatrix.applyMatrix(Mat4(rotationMat(orientation)))
      currentMatrix.rotateX(-90.0f)
      glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f)
      seq(0) = currentMatrix.current
      glUniformMatrix4(modelToCameraUnif, false, seq.buffer)
      ship.render("tint")
    }
    glUseProgram(0)

  }

  def offsetOrientation(axis: inVec3, angleDeg: Float) {
    val angleRad = radians(angleDeg)
    val normAxis = normalize(axis)

    normAxis *= sin(angleRad / 2.0f)
    val scalar = cos(angleRad / 2.0f)

    val offset = Quat4(scalar, normAxis.x, normAxis.y, normAxis.z)
    
    offsetRelative match {
      case o if o == modelRelative =>
      {
        orientation := orientation * offset
      }
      case o if o == worldRelative => 
      {
        orientation := offset * orientation
      }
      case o if o == cameraRelative =>
      {
        val camPos = resolveCamPositon
        val camMat = calcLookAtMatrix(camPos, camTarget, Vec3.UnitY)

        val camMat3 = Mat3(camMat)
        val viewQuat = quaternion(camMat3)
        val invViewQuat = conjugate(viewQuat)

        val worldQuat = invViewQuat * offset * viewQuat
        orientation := worldQuat * orientation
      }
    }

    orientation := normalize(orientation)
  }
  
  def reshape(width: Int, height: Int) {

    val ms = new MatrixStack
    val zNear = 1.0f
    val zFar = 600.0f
    ms.perspective(20.0f, height / width.toFloat, zNear, zFar)

    seq(0) = ms.current

    glUseProgram(program)
    glUniformMatrix4(cameraToClipUnif, false, seq.buffer)
    glUseProgram(0)

    glViewport(0, 0, width, height)
  }


  def input {
    val smallInc = 9.0f
    while (Keyboard.next()) {
      if(Keyboard.getEventKeyState) {
        val key = Keyboard.getEventCharacter()
        key match {
          case 'w' => offsetOrientation(Vec3.UnitX, smallInc)
          case 's' => offsetOrientation(Vec3.UnitX, -smallInc)
          case 'a' => offsetOrientation(Vec3.UnitY, smallInc)
          case 'd' => offsetOrientation(Vec3.UnitY, -smallInc)
          case 'q' => offsetOrientation(Vec3.UnitZ, smallInc)
          case 'e' => offsetOrientation(Vec3.UnitZ, -smallInc)
          case 'i' => sphereCamRelPos.y -= 11.25f
          case 'k' => sphereCamRelPos.y += 11.25f
          case 'j' => sphereCamRelPos.x -= 11.25f
          case 'l' => sphereCamRelPos.x += 11.25f
          case 'I' => sphereCamRelPos.y -= 1.125f
          case 'K' => sphereCamRelPos.y += 1.125f
          case 'J' => sphereCamRelPos.x -= 1.125f
          case 'L' => sphereCamRelPos.x += 1.125f
          case ' ' => offsetRelative = (offsetRelative + 1) % 3
          case _ => 
        }
      }

      sphereCamRelPos.y = clamp(sphereCamRelPos.y, -78.75f, 10.0f)
    }
  }

}
