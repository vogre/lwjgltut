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

import simplex3d.math._
import simplex3d.math.float._
import simplex3d.math.float.functions._

import simplex3d.data._
import simplex3d.data.float._


class Tutorial7WorldScene extends Tutorial {

  class ProgramData(val program: Int, 
                  val modelToWorldMatrixUnif: Int,
                  val worldToCameraMatrixUnif: Int,
                  val cameraToClipMatrixUnif: Int,
                  val baseColorUnif: Int)

  override val name = "World Scene"

  var uniformColor: ProgramData = _
  var objectColor: ProgramData = _
  var uniformColorTint: ProgramData = _

  val zNear = 1.0f
  val zFar = 1000.0f
  
  val seq = DataBuffer[Mat4, RFloat](1)

  var coneMesh: Mesh = _
  var planeMesh: Mesh = _
  var cylinderMesh: Mesh = _
  var cubeColorMesh: Mesh = _
  var cubeTintMesh: Mesh = _

  var sphereCamRelPos = Vec3(67.5f, -46.0f, 150.0f)
  var camTarget = Vec3(0.0f, 0.4f, 0.0f)

  var drawLookAtPoint = false


  def calcFrustumScale(fovDeg: Float) = {
    val fovRad = radians(fovDeg)
    val scale = 1.0f / tan(fovRad / 2.0f)
    scale
  }

  val frustumScale = calcFrustumScale(45.0f)

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
    uniformColorTint = loadProgram("data/tut7/PosColorWorldTransform.vert", "data/tut7/ColorMultUniform.frag")

    coneMesh = Mesh.load("data/tut7/UnitConeTint.xml")
    planeMesh = Mesh.load("data/tut7/UnitPlane.xml")
    cylinderMesh = Mesh.load("data/tut7/UnitCylinderTint.xml")
    cubeColorMesh = Mesh.load("data/tut7/UnitCubeColor.xml")
    cubeTintMesh = Mesh.load("data/tut7/UnitCubeTint.xml")


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

    val camPos = resolveCamPositon

    val camMatrix = new MatrixStack
    camMatrix.setMatrix(calcLookAtMatrix(camPos, camTarget, Vec3.UnitY))

    seq(0) = camMatrix.current

    glUseProgram(uniformColor.program)
    glUniformMatrix4(uniformColor.worldToCameraMatrixUnif, false, seq.buffer)
    
    glUseProgram(objectColor.program)
    glUniformMatrix4(objectColor.worldToCameraMatrixUnif, false, seq.buffer)
    
    glUseProgram(uniformColorTint.program)
    glUniformMatrix4(uniformColorTint.worldToCameraMatrixUnif, false, seq.buffer)
    glUseProgram(0)

    val modelMatrix = new MatrixStack

    //render the ground plane.
    {
      modelMatrix.withMatrix {
        modelMatrix.scale(Vec3(100.0f, 1.0f, 100.0f))

        glUseProgram(uniformColor.program)
        seq(0) = modelMatrix.current

        glUniformMatrix4(uniformColor.modelToWorldMatrixUnif, false, seq.buffer)
        glUniform4f(uniformColor.baseColorUnif, 0.302f, 0.416f, 0.0589f, 1.0f)
        planeMesh.render
        glUseProgram(0)
      }
    }

    drawForest(modelMatrix)

    modelMatrix.withMatrix {
      modelMatrix.translate(Vec3(20.0f, 0.0f, -10.0f))
      drawParthenon(modelMatrix)
    }

    if (drawLookAtPoint) {
      glDisable(GL_DEPTH_TEST)
      val identity = Mat4(1.0f)

      modelMatrix.withMatrix {
        val cameraAimVec = camTarget - camPos
        modelMatrix.translate(Vec3(0.0f, 0.0f, -length(cameraAimVec)))
        modelMatrix.scale(Vec3(1.0f, 1.0f, 1.0f))
        glUseProgram(objectColor.program)
        seq(0) = modelMatrix.current
        glUniformMatrix4(objectColor.modelToWorldMatrixUnif, false, seq.buffer)
        seq(0) = identity
        glUniformMatrix4(objectColor.worldToCameraMatrixUnif, false, seq.buffer)

        cubeColorMesh.render

      }
      
      glUseProgram(0)
      glEnable(GL_DEPTH_TEST)

    }

  }
  
  def reshape(width: Int, height: Int) {

    val ms = new MatrixStack
    ms.perspective(45.0f, height / width.toFloat, zNear, zFar)

    seq(0) = ms.current

    glUseProgram(uniformColor.program)
    glUniformMatrix4(uniformColor.cameraToClipMatrixUnif, false, seq.buffer)
    
    glUseProgram(objectColor.program)
    glUniformMatrix4(objectColor.cameraToClipMatrixUnif, false, seq.buffer)
    
    glUseProgram(uniformColorTint.program)
    glUniformMatrix4(uniformColorTint.cameraToClipMatrixUnif, false, seq.buffer)

    glUseProgram(0)
  }

  def drawForest(modelMatrix: MatrixStack) {
    for(t <- treeData) {
      modelMatrix.withMatrix {
        modelMatrix.translate(Vec3(t.xPos, 0.0f, t.zPos))
        drawTree(modelMatrix, t.treeHeight, t.coneHeight)
      }
    }
  }

  def drawTree(modelMatrix: MatrixStack, trunkHeight: Float, coneHeight: Float) {
    modelMatrix.withMatrix {
      modelMatrix.scale(Vec3(1.0f, trunkHeight, 1.0f))
      modelMatrix.translate(Vec3(0.0f, 0.5f, 1.0f))

      glUseProgram(uniformColorTint.program)
      seq(0) = modelMatrix.current
      glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, seq.buffer)
      glUniform4f(uniformColorTint.baseColorUnif, 0.694f, 0.4f, 0.104f, 1.0f)

      cylinderMesh.render
      glUseProgram(0)
    }
    
    modelMatrix.withMatrix {
      modelMatrix.translate(Vec3(0.0f, trunkHeight, 1.0f))
      modelMatrix.scale(Vec3(3.0f, coneHeight, 3.0f))

      glUseProgram(uniformColorTint.program)
      seq(0) = modelMatrix.current
      glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, seq.buffer)
      glUniform4f(uniformColorTint.baseColorUnif, 0.0f, 1.0f, 0.0f, 1.0f)
      
      coneMesh.render
      glUseProgram(0)
    }
  }

  def drawParthenon(modelMatrix: MatrixStack) {
    val parthenonWidth = 14.0f;
    val parthenonLength = 20.0f;
    val parthenonBaseHeight = 1.0f;
    val parthenonColumnHeight = 5.0f;
    val parthenonTopHeight = 2.0f;

    //render base

    modelMatrix.withMatrix {
      modelMatrix.scale(Vec3(parthenonWidth, parthenonBaseHeight, parthenonLength))
      modelMatrix.translate(Vec3(0.0f, 0.5f, 0.0f))

      glUseProgram(uniformColorTint.program)
      seq(0) = modelMatrix.current
      glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, seq.buffer)
      glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f)
      cubeTintMesh.render
      glUseProgram(0)
    }
    
    modelMatrix.withMatrix {
      modelMatrix.translate(Vec3(0.0f, parthenonBaseHeight + parthenonColumnHeight, 0.0f))
      modelMatrix.scale(Vec3(parthenonWidth, parthenonTopHeight, parthenonLength))
      modelMatrix.translate(Vec3(0.0f, 0.5f, 0.0f))

      glUseProgram(uniformColorTint.program)
      seq(0) = modelMatrix.current
      glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, seq.buffer)
      glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f)
      cubeTintMesh.render
      glUseProgram(0)
    }

    val frontZVal = parthenonLength / 2.0f - 1.0f
    val rightXVal = parthenonWidth / 2.0f - 1.0f

    //draw columns
    for (columnNum <- 0 until (parthenonWidth / 2.0f).toInt) {
      modelMatrix.withMatrix {
        modelMatrix.translate(Vec3(2.0f * columnNum - (parthenonWidth / 2.0f) + 1.0f,
                              parthenonBaseHeight,
                              frontZVal))
        drawColumn(modelMatrix, parthenonColumnHeight)
      }
      modelMatrix.withMatrix {
        modelMatrix.translate(Vec3(2.0f * columnNum - (parthenonWidth / 2.0f) + 1.0f,
                              parthenonBaseHeight,
                              -frontZVal))
        drawColumn(modelMatrix, parthenonColumnHeight)
      }
    }

    //don't draw first and last columns since they've been drawn already 
    for (columnNum <- 1 until ((parthenonLength - 2.0f) / 2.0f).toInt) {
      modelMatrix.withMatrix {
        modelMatrix.translate(Vec3(rightXVal,
                                   parthenonBaseHeight,
                                   2.0f * columnNum - (parthenonLength / 2.0f) + 1.0f))
        drawColumn(modelMatrix, parthenonColumnHeight)
      }
      modelMatrix.withMatrix {
        modelMatrix.translate(Vec3(-rightXVal,
                                   parthenonBaseHeight,
                                   2.0f * columnNum - (parthenonLength / 2.0f) + 1.0f))
        drawColumn(modelMatrix, parthenonColumnHeight)
      }

    }

    //draw interior
    modelMatrix.withMatrix {
      modelMatrix.translate(Vec3(0.0f, 1.0f, 0.0f))
      modelMatrix.scale(Vec3(parthenonWidth - 6.0f, parthenonColumnHeight, parthenonLength - 6.0f))
      modelMatrix.translate(Vec3(0.0f, 0.5f, 0.0f))

      glUseProgram(objectColor.program)
      seq(0) = modelMatrix.current
      glUniformMatrix4(objectColor.modelToWorldMatrixUnif, false, seq.buffer)
      cubeColorMesh.render
      glUseProgram(0)
    }

    //draw headpiece
    modelMatrix.withMatrix {
      modelMatrix.translate(Vec3(0.0f,
                                 parthenonColumnHeight + parthenonBaseHeight + (parthenonTopHeight / 2.0f),
                                 parthenonLength / 2.0f))
      modelMatrix.rotateX(-135.0f)
      modelMatrix.rotateY(45.0f)
      
      glUseProgram(objectColor.program)
      seq(0) = modelMatrix.current
      glUniformMatrix4(objectColor.modelToWorldMatrixUnif, false, seq.buffer)
      cubeColorMesh.render
      glUseProgram(0)
    }

  }

  def drawColumn(modelMatrix: MatrixStack, height: Float) {
    val columnBaseHeight = 0.25f

    //draw the bottom of the column

    modelMatrix.withMatrix {
      modelMatrix.scale(Vec3(1.0f, columnBaseHeight, 1.0f))
      modelMatrix.translate(Vec3(0.0f, 0.5f, 0.0f))

      glUseProgram(uniformColorTint.program)
      seq(0) = modelMatrix.current
      glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, seq.buffer)
      glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f)
      cubeTintMesh.render
      glUseProgram(0)
    }
    
    //draw the top of the column
    modelMatrix.withMatrix {
      modelMatrix.translate(Vec3(0.0f, height - columnBaseHeight, 0.0f))
      modelMatrix.scale(Vec3(1.0f, columnBaseHeight, 1.0f))
      modelMatrix.translate(Vec3(0.0f, 0.5f, 0.0f))

      glUseProgram(uniformColorTint.program)
      seq(0) = modelMatrix.current
      glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, seq.buffer)
      glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f)
      cubeTintMesh.render
      glUseProgram(0)
    }

    //draw the main column
    modelMatrix.withMatrix {
      modelMatrix.translate(Vec3(0.0f, columnBaseHeight, 0.0f))
      modelMatrix.scale(Vec3(0.8f, height - (columnBaseHeight * 2.0f), 0.8f))
      modelMatrix.translate(Vec3(0.0f, 0.5f, 0.0f))

      glUseProgram(uniformColorTint.program)
      seq(0) = modelMatrix.current
      glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, seq.buffer)
      glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f)
      cylinderMesh.render
      glUseProgram(0)
    }

  }

  def input {
    while (Keyboard.next()) {
      if(Keyboard.getEventKeyState) {
        val key = Keyboard.getEventCharacter()
        key match {
          case 'w' => camTarget.z -= 4.0f
          case 's' => camTarget.z += 4.0f
          case 'd' => camTarget.x += 4.0f
          case 'a' => camTarget.x -= 4.0f
          case 'q' => camTarget.y -= 4.0f
          case 'e' => camTarget.y += 4.0f
          case 'i' => sphereCamRelPos.y -= 11.25f
          case 'k' => sphereCamRelPos.y += 11.25f
          case 'j' => sphereCamRelPos.x -= 11.25f
          case 'l' => sphereCamRelPos.x += 11.25f
          case 'o' => sphereCamRelPos.z -= 11.25f
          case 'u' => sphereCamRelPos.z += 11.25f
          case ' ' => drawLookAtPoint = !drawLookAtPoint
          case _ =>
        }
        sphereCamRelPos.y = clamp(sphereCamRelPos.y, -78.75f, -1.0f)
        camTarget.y = if (camTarget.y > 0.0f) camTarget.y else 0.0f
        sphereCamRelPos.z = if (sphereCamRelPos.z > 5.0f) sphereCamRelPos.z else 5.0f
      }
    }
  }

  class TreeData(val xPos: Float, val zPos: Float, val treeHeight: Float, val coneHeight: Float)

  val treeData = Array(
    new TreeData(-45.0f, -40.0f, 2.0f, 3.0f),
    new TreeData(-42.0f, -35.0f, 2.0f, 3.0f),
    new TreeData(-39.0f, -29.0f, 2.0f, 4.0f),
    new TreeData(-44.0f, -26.0f, 3.0f, 3.0f),
    new TreeData(-40.0f, -22.0f, 2.0f, 4.0f),
    new TreeData(-36.0f, -15.0f, 3.0f, 3.0f),
    new TreeData(-41.0f, -11.0f, 2.0f, 3.0f),
    new TreeData(-37.0f, -6.0f, 3.0f, 3.0f),
    new TreeData(-45.0f, 0.0f, 2.0f, 3.0f),
    new TreeData(-39.0f, 4.0f, 3.0f, 4.0f),
    new TreeData(-36.0f, 8.0f, 2.0f, 3.0f),
    new TreeData(-44.0f, 13.0f, 3.0f, 3.0f),
    new TreeData(-42.0f, 17.0f, 2.0f, 3.0f),
    new TreeData(-38.0f, 23.0f, 3.0f, 4.0f),
    new TreeData(-41.0f, 27.0f, 2.0f, 3.0f),
    new TreeData(-39.0f, 32.0f, 3.0f, 3.0f),
    new TreeData(-44.0f, 37.0f, 3.0f, 4.0f),
    new TreeData(-36.0f, 42.0f, 2.0f, 3.0f),

    new TreeData(-32.0f, -45.0f, 2.0f, 3.0f),
    new TreeData(-30.0f, -42.0f, 2.0f, 4.0f),
    new TreeData(-34.0f, -38.0f, 3.0f, 5.0f),
    new TreeData(-33.0f, -35.0f, 3.0f, 4.0f),
    new TreeData(-29.0f, -28.0f, 2.0f, 3.0f),
    new TreeData(-26.0f, -25.0f, 3.0f, 5.0f),
    new TreeData(-35.0f, -21.0f, 3.0f, 4.0f),
    new TreeData(-31.0f, -17.0f, 3.0f, 3.0f),
    new TreeData(-28.0f, -12.0f, 2.0f, 4.0f),
    new TreeData(-29.0f, -7.0f, 3.0f, 3.0f),
    new TreeData(-26.0f, -1.0f, 2.0f, 4.0f),
    new TreeData(-32.0f, 6.0f, 2.0f, 3.0f),
    new TreeData(-30.0f, 10.0f, 3.0f, 5.0f),
    new TreeData(-33.0f, 14.0f, 2.0f, 4.0f),
    new TreeData(-35.0f, 19.0f, 3.0f, 4.0f),
    new TreeData(-28.0f, 22.0f, 2.0f, 3.0f),
    new TreeData(-33.0f, 26.0f, 3.0f, 3.0f),
    new TreeData(-29.0f, 31.0f, 3.0f, 4.0f),
    new TreeData(-32.0f, 38.0f, 2.0f, 3.0f),
    new TreeData(-27.0f, 41.0f, 3.0f, 4.0f),
    new TreeData(-31.0f, 45.0f, 2.0f, 4.0f),
    new TreeData(-28.0f, 48.0f, 3.0f, 5.0f),

    new TreeData(-25.0f, -48.0f, 2.0f, 3.0f),
    new TreeData(-20.0f, -42.0f, 3.0f, 4.0f),
    new TreeData(-22.0f, -39.0f, 2.0f, 3.0f),
    new TreeData(-19.0f, -34.0f, 2.0f, 3.0f),
    new TreeData(-23.0f, -30.0f, 3.0f, 4.0f),
    new TreeData(-24.0f, -24.0f, 2.0f, 3.0f),
    new TreeData(-16.0f, -21.0f, 2.0f, 3.0f),
    new TreeData(-17.0f, -17.0f, 3.0f, 3.0f),
    new TreeData(-25.0f, -13.0f, 2.0f, 4.0f),
    new TreeData(-23.0f, -8.0f, 2.0f, 3.0f),
    new TreeData(-17.0f, -2.0f, 3.0f, 3.0f),
    new TreeData(-16.0f, 1.0f, 2.0f, 3.0f),
    new TreeData(-19.0f, 4.0f, 3.0f, 3.0f),
    new TreeData(-22.0f, 8.0f, 2.0f, 4.0f),
    new TreeData(-21.0f, 14.0f, 2.0f, 3.0f),
    new TreeData(-16.0f, 19.0f, 2.0f, 3.0f),
    new TreeData(-23.0f, 24.0f, 3.0f, 3.0f),
    new TreeData(-18.0f, 28.0f, 2.0f, 4.0f),
    new TreeData(-24.0f, 31.0f, 2.0f, 3.0f),
    new TreeData(-20.0f, 36.0f, 2.0f, 3.0f),
    new TreeData(-22.0f, 41.0f, 3.0f, 3.0f),
    new TreeData(-21.0f, 45.0f, 2.0f, 3.0f),

    new TreeData(-12.0f, -40.0f, 2.0f, 4.0f),
    new TreeData(-11.0f, -35.0f, 3.0f, 3.0f),
    new TreeData(-10.0f, -29.0f, 1.0f, 3.0f),
    new TreeData(-9.0f, -26.0f, 2.0f, 2.0f),
    new TreeData(-6.0f, -22.0f, 2.0f, 3.0f),
    new TreeData(-15.0f, -15.0f, 1.0f, 3.0f),
    new TreeData(-8.0f, -11.0f, 2.0f, 3.0f),
    new TreeData(-14.0f, -6.0f, 2.0f, 4.0f),
    new TreeData(-12.0f, 0.0f, 2.0f, 3.0f),
    new TreeData(-7.0f, 4.0f, 2.0f, 2.0f),
    new TreeData(-13.0f, 8.0f, 2.0f, 2.0f),
    new TreeData(-9.0f, 13.0f, 1.0f, 3.0f),
    new TreeData(-13.0f, 17.0f, 3.0f, 4.0f),
    new TreeData(-6.0f, 23.0f, 2.0f, 3.0f),
    new TreeData(-12.0f, 27.0f, 1.0f, 2.0f),
    new TreeData(-8.0f, 32.0f, 2.0f, 3.0f),
    new TreeData(-10.0f, 37.0f, 3.0f, 3.0f),
    new TreeData(-11.0f, 42.0f, 2.0f, 2.0f),


    new TreeData(15.0f, 5.0f, 2.0f, 3.0f),
    new TreeData(15.0f, 10.0f, 2.0f, 3.0f),
    new TreeData(15.0f, 15.0f, 2.0f, 3.0f),
    new TreeData(15.0f, 20.0f, 2.0f, 3.0f),
    new TreeData(15.0f, 25.0f, 2.0f, 3.0f),
    new TreeData(15.0f, 30.0f, 2.0f, 3.0f),
    new TreeData(15.0f, 35.0f, 2.0f, 3.0f),
    new TreeData(15.0f, 40.0f, 2.0f, 3.0f),
    new TreeData(15.0f, 45.0f, 2.0f, 3.0f),

    new TreeData(25.0f, 5.0f, 2.0f, 3.0f),
    new TreeData(25.0f, 10.0f, 2.0f, 3.0f),
    new TreeData(25.0f, 15.0f, 2.0f, 3.0f),
    new TreeData(25.0f, 20.0f, 2.0f, 3.0f),
    new TreeData(25.0f, 25.0f, 2.0f, 3.0f),
    new TreeData(25.0f, 30.0f, 2.0f, 3.0f),
    new TreeData(25.0f, 35.0f, 2.0f, 3.0f),
    new TreeData(25.0f, 40.0f, 2.0f, 3.0f),
    new TreeData(25.0f, 45.0f, 2.0f, 3.0f))

}
