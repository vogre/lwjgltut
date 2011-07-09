package lwjgltut.framework

import simplex3d.math._
import simplex3d.math.float._
import simplex3d.math.float.functions._

import simplex3d.data._
import simplex3d.data.float._

import scala.collection.mutable.Stack

class MatrixStack {

  private[this] var currentMatrix = Mat4(1.0f)

  def current = currentMatrix

  val matrixStack = new Stack[Mat4]()

  def rotate(rotateAxis: Vec3, angleDeg: Float) {
    val angleRad = radians(angleDeg)
    rotateRadians(rotateAxis, angleRad)
  }

  def rotateRadians(rotateAxis: Vec3, angRad: Float) = {
    val rCos = cos(angRad)
    val rSin = sin(angRad)
    val invCos = 1.0f - rCos
    val invSin = 1.0f - rSin
    val theMat = Mat4(1.0f)
    val axis = normalize(rotateAxis)
    theMat(0, 0) = (axis.x * axis.x) + ((1 - axis.x * axis.x) * rCos)
    theMat(0, 1) = axis.x * axis.y * (invCos) - (axis.z * rSin)
    theMat(0, 2) = axis.x * axis.z * (invCos) + (axis.y * rSin)

    theMat(1, 0) = axis.x * axis.y * (invCos) + (axis.z * rSin)
    theMat(1, 1) = (axis.y * axis.y) + ((1 - axis.y * axis.y) * rCos)
    theMat(1, 2) = axis.y * axis.z * (invCos) - (axis.x * rSin)

    theMat(2, 0) = axis.x * axis.z * (invCos) - (axis.y * rSin)
    theMat(2, 1) = axis.y * axis.z * (invCos) + (axis.x * rSin)
    theMat(2, 2) = (axis.z * axis.z) + ((1 - axis.z * axis.z) * rCos)

    currentMatrix *= theMat
  }

  def rotateX(angDeg: Float) {
    val angRad = radians(angDeg)
    val rcos = cos(angRad)
    val rsin = sin(angRad)
    val mat = Mat4(1.0f)
    mat(1, 1) = rcos
    mat(1, 2) = -rsin
    mat(2, 1) = rsin
    mat(2, 1) = rcos
    currentMatrix *= mat
  }
  
  def rotateY(angDeg: Float) {
    val angRad = radians(angDeg)
    val rcos = cos(angRad)
    val rsin = sin(angRad)
    val mat = Mat4(1.0f)
    mat(0, 0) = rcos
    mat(0, 2) = rsin
    mat(2, 0) = -rsin
    mat(2, 2) = rcos
    currentMatrix *= mat
  }

  def rotateZ(angDeg: Float) {
    val angRad = radians(angDeg)
    val rcos = cos(angRad)
    val rsin = sin(angRad)
    val mat = Mat4(1.0f)
    mat(0, 0) = rcos
    mat(0, 1) = -rsin
    mat(1, 0) = rsin
    mat(1, 1) = rcos
    currentMatrix *= mat
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

  def applyMatrix(mat: Mat4) {
    currentMatrix *= mat
  }

  def setIdentity {
    currentMatrix = Mat4(1.0f)
  }
  
  def setMatrix(mat: Mat4) {
    currentMatrix = mat
  }

  def perspective(fieldOfViewDeg: Float, aspectRatio: Float, near: Float, far: Float) {
    currentMatrix *= perspectiveProj(radians(fieldOfViewDeg), aspectRatio, near, far)
  }

  def withMatrix(op: => Unit) {
      push
      op
      pop
  }

  def push {
    matrixStack.push(currentMatrix.clone)
  }

  def pop {
    currentMatrix = matrixStack.top
    matrixStack.pop
  }
}
