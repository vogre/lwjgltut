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

class Tutorial8QuaternionYPR extends Tutorial {

  override val name = "Quaternion YPR"

  var program = 0
  
  var modelToCameraUnif = 0
  var cameraToClipUnif = 0
  var baseColorUnif = 0

  var ship : Mesh = _

  var orientation: Quat4 = Quat4.Identity

  var rightMultiply = true

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
  

  def display {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClearDepth(1.0f)
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    val currentMatrix = new MatrixStack
    currentMatrix.translate(Vec3(0.0f, 0.0f, -200.0f))
    currentMatrix.applyMatrix(Mat4(rotationMat(orientation)))

    glUseProgram(program)
    currentMatrix.scale(Vec3(3.0f, 3.0f, 3.0f))
    currentMatrix.rotateX(-90.0f)
    glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f)
    seq(0) = currentMatrix.current
    glUniformMatrix4(modelToCameraUnif, false, seq.buffer)
    ship.render("tint")
    glUseProgram(0)

  }

  def offsetOrientation(axis: inVec3, angleDeg: Float) {
    val angleRad = radians(angleDeg)
    val normAxis = normalize(axis)

    normAxis *= sin(angleRad / 2.0f)
    val scalar = cos(angleRad / 2.0f)

    val offset = Quat4(scalar, normAxis.x, normAxis.y, normAxis.z)
    
    if (rightMultiply) {
      orientation := orientation * offset
    } else {
      orientation := offset * orientation
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
          case ' ' => rightMultiply = !rightMultiply
          case _ => 
        }
      }
    }
  }

}
