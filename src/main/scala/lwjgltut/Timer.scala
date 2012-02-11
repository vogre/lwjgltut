package lwjgltut.framework

import simplex3d.math.float.functions._

object TimerType extends Enumeration {
  type TimerType = Value
  val Loop = Value
  val Single = Value
  val Infinite = Value
}

class Timer(val timerType: TimerType.TimerType = TimerType.Infinite, val duration: Float = 1.0f) {

  var paused = false
  var updated = false

  var absPrevTime = 0.0f
  var secAccumTime = 0.0f

  if (timerType != TimerType.Infinite)
    require(duration > 0.0f)

  def reset {
    updated = false
    secAccumTime = 0.0f
  }

  def togglePause = {
    paused = !paused
    paused
  }

  def setPause(pause: Boolean = true) {
    paused = pause
  }

  def isPaused = paused

  def update = {
    val absCurTime = System.nanoTime / 1000000000.0f
    if (!updated) {
      updated = true
      absPrevTime = absCurTime
    }
    if (paused) {
      absPrevTime = absCurTime
      false
    } else {
      val deltaTime = absCurTime - absPrevTime
      secAccumTime += deltaTime
      absPrevTime = absCurTime
      if (timerType == TimerType.Single) secAccumTime > duration
      else false
    }

  }

  def rewind(secRewind: Float) {
    secAccumTime -= secRewind
    if (secAccumTime < 0.0f) secAccumTime = 0.0f
  }

  def fastForward(secForward: Float) {
    secAccumTime += secForward
  }

  def alpha = timerType match {
    case TimerType.Loop => (secAccumTime % duration) / duration
    case TimerType.Single => clamp(secAccumTime / duration, 0.0f, 1.0f)
    case _ => -1.0f
  }

  def progression = timerType match {
    case TimerType.Loop => (secAccumTime % duration)
    case TimerType.Single => clamp(secAccumTime / duration, 0.0f, duration)
    case _ => -1.0
  }

  def timeSinceStart = secAccumTime
}
