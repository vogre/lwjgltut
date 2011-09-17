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

class Tutorial8GimbalLock extends Tutorial {


  override val name = "World Scene"

  var program = 0
  
  var modelToCameraUnif = 0
  var cameraToClipUnif = 0
  var baseColorUnif = 0

  var drawGimbals = true

  var ship : Mesh = _

  var gimbals: Map[GimbalAxis, Mesh] = _

  case class GimbalAxis(id: Int)

  val xAxis = GimbalAxis(0)
  val yAxis = GimbalAxis(1)
  val zAxis = GimbalAxis(2)

  val seq = DataBuffer[Mat4, RFloat](1)

  var cameraToClipMat = Mat4(1.0f)

  var angleX = 0.0f
  var angleY = 0.0f
  var angleZ = 0.0f


  def init {
    val vert = "data/tut8/PosColorLocalTransform.vert".compile
    val frag = "data/tut8/ColorMultUniform.frag".compile
    program = createProgram(List(vert, frag))

    modelToCameraUnif = glGetUniformLocation(program, "modelToCameraMatrix")
    cameraToClipUnif = glGetUniformLocation(program, "cameraToClipMatrix")
    baseColorUnif = glGetUniformLocation(program, "baseColor")

    val xGimbal = Mesh.load("data/tut8/LargeGimbal.xml")
    val yGimbal = Mesh.load("data/tut8/MediumGimbal.xml")
    val zGimbal = Mesh.load("data/tut8/SmallGimbal.xml")

    ship = Mesh.load("data/tut8/Ship.xml")

    gimbals = Map(xAxis -> xGimbal, yAxis -> yGimbal, zAxis -> zGimbal)


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
    currentMatrix.rotateX(angleX)
    drawGimbal(currentMatrix, xAxis, Vec4(0.4f, 0.4f, 1.0f, 1.0f))
    currentMatrix.rotateY(angleY)
    drawGimbal(currentMatrix, yAxis, Vec4(0.0f, 1.0f, 0.0f, 1.0f))
    currentMatrix.rotateZ(angleZ)
    drawGimbal(currentMatrix, zAxis, Vec4(1.0f, 0.3f, 0.3f, 1.0f))

    glUseProgram(program)
    currentMatrix.scale(Vec3(3.0f, 3.0f, 3.0f))
    currentMatrix.rotateX(-90.0f)
    glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f)
    seq(0) = currentMatrix.current
    glUniformMatrix4(modelToCameraUnif, false, seq.buffer)
    ship.render("tint")
    glUseProgram(0)

  }

  def drawGimbal(currentMatrix: MatrixStack, axis: GimbalAxis, baseColor: Vec4) {
    if (!drawGimbals) 
      return
    currentMatrix.withMatrix {
      axis match {
        case GimbalAxis(0) => 
        case GimbalAxis(1) => {
          currentMatrix.rotateZ(90.0f)
          currentMatrix.rotateX(90.0f)
        }
        case GimbalAxis(2) => {
          currentMatrix.rotateY(90.0f)
          currentMatrix.rotateX(90.0f)
        }
      }

      glUseProgram(program)
      val colorBuf = DataBuffer[Vec4, RFloat](1)
      colorBuf(0) = baseColor
      glUniform4(baseColorUnif, colorBuf.buffer)
      seq(0) = currentMatrix.current
      glUniformMatrix4(modelToCameraUnif, false, seq.buffer)
      gimbals(axis).render
      glUseProgram(0)

    }
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
          case 'w' => angleX += smallInc
          case 's' => angleX -= smallInc
          case 'a' => angleY -= smallInc
          case 'd' => angleY += smallInc
          case 'q' => angleZ -= smallInc
          case 'e' => angleZ += smallInc
          case ' ' => drawGimbals = !drawGimbals
          case _ =>
        }
      }
    }
  }

}
