package com.github.vogre 

import org.lwjgl.opengl.{Display, DisplayMode, ContextAttribs, PixelFormat, GL11}
import GL11._
import org.lwjgl.input.Keyboard

object Framework {
  def play(tutorial: Tutorial) {
    try {
      Display.setTitle(tutorial.name)
      Display.setDisplayMode(new DisplayMode(500, 500))
      Display.create
      tutorial.init
      while(!Display.isCloseRequested && !tutorial.finished) {
        Display.update
        if(Display.isActive) {
          tutorial.display
          Display.sync(60)
        } else {
          Thread.sleep(100)
        }
        tutorial.input

        val lastError = glGetError
        if (lastError != GL_NO_ERROR) {
          println(lastError)
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
          return
        }
      }
    } finally {
      Display.destroy()
    }
  }
}
