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

object Tut8Orients {
  val orientations = Array(
    Quat4(0.7071f, 0.7071f, 0.0f, 0.0f),
    Quat4(0.5f, 0.5f, -0.5f, 0.5f),
    Quat4(-0.4895f, -0.7892f, -0.3700f, -0.02514f),
    Quat4(0.4895f, 0.7892f, 0.3700f, 0.02514f),

    Quat4(0.3840f, -0.1591f, -0.7991f, -0.4344f),
    Quat4(0.5537f, 0.5208f, 0.6483f, 0.0410f),
    Quat4(0.0f, 0.0f, 1.0f, 0.0f)
  )

}

class Animation() {
  private var timer : Timer = _

  var finalOrient = 0

  def finalOrientation = finalOrient

  def updateTime = timer.update

  def vectorize(quat: inQuat4) = Vec4(quat.a, quat.b, quat.c, quat.d)


  def slerp(first: inQuat4, second: inQuat4, alpha: Float) = {
    var dotVal = dot (vectorize(first), vectorize(second))
    if (dotVal > 0.9995f) lerp(first, second, alpha)
    else {
      dotVal = clamp(dotVal, -1.0f, 1.0f)
      val theta0 = acos(dotVal).toFloat
      val theta = theta0 * alpha

      var v2 = second - (first*dotVal)
      v2 = normalize(v2)

      first*(cos(theta).toFloat) + v2*(sin(theta).toFloat)
    }
  }

  def lerp(first: inQuat4, second: inQuat4, alpha: Float) = {
    var start = vectorize(first)
    var end = vectorize(second)
    var interp = mix(start, end, alpha)
    interp = normalize(interp)
    Quat4(interp.x, interp.y, interp.z, interp.w)
  }

  def getOrient(initial: inQuat4, slerp: Boolean) = slerp match {
    case true => this.slerp(initial, Tut8Orients.orientations(finalOrient), timer.alpha)
    case false => this.lerp(initial, Tut8Orients.orientations(finalOrient), timer.alpha)
  }

  def startOrienting(ixDestination: Int, duration: Float) {
    finalOrient = ixDestination
    timer = new Timer(TimerType.Single, duration)
  }

}

class Orientation {
  var isAnimating = false
  var ixCurOrientation = 0
  var slerp = false
  var anim = new Animation()

  def toggleSlerp = {
    slerp = !slerp
    slerp
  }

  def getOrient = {
    if (isAnimating) {
      anim.getOrient(Tut8Orients.orientations(ixCurOrientation), slerp)
    } else {
      Tut8Orients.orientations(ixCurOrientation)
    }
  }

  def animating = isAnimating

  def updateTime {
    if (isAnimating) {
      val isFinished = anim.updateTime
      if (isFinished) {
        isAnimating = false
        ixCurOrientation = anim.finalOrientation
      }
    }
  }

  def animateToOrient(ixDestination: Int) {
    if (ixCurOrientation == ixDestination) return
    anim.startOrienting(ixDestination, 1.0f)
    isAnimating = true
  }

}

class Tutorial8Interpolation extends Tutorial {

  /*class Animation {

  }*/

  override val name = "Interpolation"

  val orient = new Orientation

  var program = 0

  var modelToCameraUnif = 0
  var cameraToClipUnif = 0
  var baseColorUnif = 0

  var ship : Mesh = _
  var plane : Mesh = _

  var orientation: Quat4 = Quat4.Identity

  val seq = DataBuffer[Mat4, RFloat](1)

  var cameraToClipMat = Mat4(1.0f)


  val orientKeys = Array('q',
    'w',
    'e',
    'r',

    't',
    'y',
    'u')

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


  def display {
    orient.updateTime

    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClearDepth(1.0f)
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    val currentMatrix = new MatrixStack
    currentMatrix.translate(Vec3(0.0f, 0.0f, -200.0f))
    currentMatrix.applyMatrix(Mat4(rotationMat(orient.getOrient)))


    glUseProgram(program)
    currentMatrix.scale(Vec3(3.0f, 3.0f, 3.0f))
    currentMatrix.rotateX(-90)
    glUniform4f(baseColorUnif, 1.0f,1.0f,1.0f,1.0f)

    ship.render("tint")
    seq(0) = currentMatrix.current
    glUniformMatrix4(modelToCameraUnif, false, seq.buffer)


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

  def applyOrientation(index: Int) {
    if(!orient.animating) {
      orient.animateToOrient(index)
    }
  }

  def input {
    val smallInc = 9.0f
    while (Keyboard.next()) {
      if(Keyboard.getEventKeyState) {
        val key = Keyboard.getEventCharacter()
        key match {
          case ' ' => orient.toggleSlerp
          case _ => {
            val ind = orientKeys.indexOf(key)
            if (ind > -1) {
              applyOrientation(ind)
            }
          }
        }
      }
    }
  }
}

