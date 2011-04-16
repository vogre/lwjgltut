package com.github.vogre 

import org.lwjgl.opengl.{Display, DisplayMode, ContextAttribs, PixelFormat}
import org.lwjgl.opengl.{GL11, GL15, GL20, GL30, GL32}
import GL11._
import GL15._
import GL20._
import GL30._
import GL32._
import org.lwjgl.input.Keyboard

object Framework {
  
  implicit def path2glslShader(file: String) = new GLSLShaderFile(file)


  def createProgram(shaderList: List[Int]) = {
    val program = glCreateProgram
    for(shader <- shaderList) { 
        glAttachShader(program, shader)
    }

    glLinkProgram(program)

    val status = glGetProgram(program, GL_LINK_STATUS)
    if (status == GL_FALSE)
    {
      val infoLogLength = glGetProgram(program, GL_INFO_LOG_LENGTH)
      val infoLog = glGetProgramInfoLog(program, infoLogLength)

      throw new Exception("Link failure: " + infoLog)
    }
    program
  }

  def play(tutorial: Tutorial) {
    try {
      Display.setTitle(tutorial.name)
      Display.setDisplayMode(new DisplayMode(500, 500))
      Display.setFullscreen(true)
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
      Keyboard.destroy()
    }
  }
}
