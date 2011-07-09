package lwjgltut.tutorial7

import org.lwjgl.opengl.{Display, GL11, GL15, GL20, GL30, GL32}
import org.lwjgl.input.Keyboard
import org.lwjgl.BufferUtils
import GL11._
import GL15._
import GL20._
import GL30._
import GL32._
import lwjgltut.framework._
import lwjgltut.Framework._
import lwjgltut.Tutorial

class ProgramData(val program: Int, 
                  val modelToWorldMatrixUnif: Int,
                  val worldToCameraMatrixUnif: Int,
                  val cameraToClipMatrixUnif: Int,
                  val baseColorUnif: Int)

class Tutorial7WorldScene extends Tutorial {

  override val name = "World Scene"

  var uniformColor: ProgramData = _
  var objectColor: ProgramData = _
  var uniformColorTint: ProgramData = _

  var coneMesh: Mesh = _

  def loadProgram(vertexShader: String, fragmentShader: String) = {
    val vert = vertexShader.compile
    val frag = fragmentShader.compile
    val program = createProgram(List(vert, frag))
    val modelToWorldMatrixUnif = glGetUniformLocation(program, "modelToWorldMatrix")
    val worldToCameraMatrixUnif = glGetUniformLocation(program, "worldToCameraMatrix")
    val cameraToClipMatrixUnif = glGetUniformLocation(program, "cameraToClipMatrix")
    val baseColorUnif = glGetUniformLocation(program, "baseColor")
    new ProgramData(program, modelToWorldMatrixUnif, worldToCameraMatrixUnif, cameraToClipMatrixUnif, baseColorUnif)
  }

  def init {
    uniformColor = loadProgram("data/tut7/PosOnlyWorldTransform.vert", "data/tut7/ColorUniform.frag")
    objectColor = loadProgram("data/tut7/PosColorWorldTransform.vert", "data/tut7/ColorPassthrough.frag")
    uniformColorTint = loadProgram("data/tut7/PosOnlyWorldTransform.vert", "data/tut7/ColorMultUniform.frag")

    coneMesh = Mesh.load("data/tut7/UnitConeTint.xml")
    glEnable(GL_CULL_FACE)
    glCullFace(GL_BACK)
    glFrontFace(GL_CW)


    glEnable(GL_DEPTH_TEST)
    glDepthMask(true)
    glDepthFunc(GL_LEQUAL)
    glDepthRange(0.0f, 1.0f)
    glEnable(GL_DEPTH_CLAMP)
  }

  def display {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glClearDepth(0.0f)
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
  }

  def input {

  }

}
