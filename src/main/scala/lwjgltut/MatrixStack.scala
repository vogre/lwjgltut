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
    theMat.m00 = (axis.x * axis.x) + ((1 - axis.x * axis.x) * rCos)
    theMat.m01 = axis.x * axis.y * (invCos) - (axis.z * rSin)
    theMat.m02 = axis.x * axis.z * (invCos) + (axis.y * rSin)

    theMat.m10 = axis.x * axis.y * (invCos) + (axis.z * rSin)
    theMat.m11 = (axis.y * axis.y) + ((1 - axis.y * axis.y) * rCos)
    theMat.m12 = axis.y * axis.z * (invCos) - (axis.x * rSin)

    theMat.m20 = axis.x * axis.z * (invCos) - (axis.y * rSin)
    theMat.m21 = axis.y * axis.z * (invCos) + (axis.x * rSin)
    theMat.m22 = (axis.z * axis.z) + ((1 - axis.z * axis.z) * rCos)

    currentMatrix *= theMat
  }

  def rotateX(angDeg: Float) {
    val angRad = radians(angDeg)
    val rcos = cos(angRad)
    val rsin = sin(angRad)
    val mat = Mat4(1.0f)
    mat.m11 = rcos
    mat.m12 = -rsin
    mat.m21 = rsin
    mat.m22 = rcos
    currentMatrix *= mat
  }
  
  def rotateY(angDeg: Float) {
    val angRad = radians(angDeg)
    val rcos = cos(angRad)
    val rsin = sin(angRad)
    val mat = Mat4(1.0f)
    mat.m00 = rcos
    mat.m02 = rsin
    mat.m20 = -rsin
    mat.m22 = rcos
    currentMatrix *= mat
  }

  def rotateZ(angDeg: Float) {
    val angRad = radians(angDeg)
    val rcos = cos(angRad)
    val rsin = sin(angRad)
    val mat = Mat4(1.0f)
    mat.m00 = rcos
    mat.m01 = -rsin
    mat.m10 = rsin
    mat.m11 = rcos
    currentMatrix *= mat
  }


  def scale(scaleVec: Vec3) {
    val scaleMat = Mat4(1.0f)
    scaleMat.m00 = scaleVec.x
    scaleMat.m11 = scaleVec.y
    scaleMat.m22 = scaleVec.z
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
    currentMatrix := mat
  }

  def perspective(fieldOfViewDeg: Float, aspectRatio: Float, near: Float, far: Float) {
    currentMatrix = perspectiveProj(radians(fieldOfViewDeg), aspectRatio, near, far)
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
