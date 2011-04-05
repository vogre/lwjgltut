package com.github.vogre 

import org.lwjgl._
import opengl.{Display,GL11,DisplayMode}
import GL11._
import input._
import math._

object GLTest{
	val GAME_TITLE = "My Game"
	val FRAMERATE = 60
	val width = 640
	val height = 480

	val player = new Player(0,0,0);

	var finished = false
	var angle = 0.0f
	var rotation = 0.0f

	def mains(args:Array[String]){
		var fullscreen = false
		for(arg <- args){
			arg match{
				case "-fullscreen" =>
					fullscreen = true
			}
		}

		init(fullscreen)
		run
	}

	def init(fullscreen:Boolean){

		println("init Display")
		Display.setTitle(GAME_TITLE)
		Display.setFullscreen(fullscreen)
		Display.setVSyncEnabled(true)
		Display.setDisplayMode(new DisplayMode(width,height))
		Display.create

		println("init gl")
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING)
		glEnable(GL_LIGHT0)
		adjustcam
	}

	def adjustcam(){
		val v = Display.getDisplayMode.getWidth.toFloat/Display.getDisplayMode.getHeight.toFloat
		printf("v:%f",v)
		glMatrixMode(GL_PROJECTION)
		glLoadIdentity
		glFrustum(-v,v,-1,1,1,100)
		glMatrixMode(GL_MODELVIEW)
	}

	def cleanup(){
		Display.destroy
	}

	def run(){
		while(!finished){
			Display.update
			
			logic
			render

			Display.sync(FRAMERATE)
		}
	}

	def logic(){
	    // in scala we can locally import all methods from Keyboard.
	    import Keyboard._
	    
		if(isKeyDown(KEY_ESCAPE))
			finished = true
		if(Display.isCloseRequested)
			finished = true

		// rx and rx store our keyboard input as direction  
		var ry = 0
		var rx = 0
		
		// keys are IKJL for up down left right

		if(isKeyDown(KEY_I))
			ry += 1
		if(isKeyDown(KEY_K))
			ry -= 1
		if(isKeyDown(KEY_J))
			rx -= 1
		if(isKeyDown(KEY_L))
			rx += 1
		
		// this makes the direction relative to the camera position
		// it is a simple rotation matrix you may know from linear algebra 
		val ax = rx*cos(-rotation.toRadians)-ry*sin(-rotation.toRadians)
		val ay = rx*sin(-rotation.toRadians)+ry*cos(-rotation.toRadians)
		
		player.x += 0.1f*ax.toFloat
		player.y += 0.1f*ay.toFloat
		
		// this rotates our camera around the center
		angle += 2.0f % 360
		rotation += 0.2f
	}

	def renderGrid(size : Int){
	    // this creates the nice looking background.
		glDisable(GL_LIGHTING)
		glBegin(GL_LINES)
		for(i <- -size to size){
			glVertex2i(i,-size)
			glVertex2i(i, size)
			glVertex2i(-size,i)
			glVertex2i( size,i)
		}
		glEnd
		glEnable(GL_LIGHTING)
	}

	def render(){
		glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glLoadIdentity

		glTranslatef(0,0,-20)
		glRotatef(-70,1,0,0)
		glRotatef(rotation,0,0,1)
		
		glPushMatrix
		player.applyPos
		//rotate the player just for fun
		glRotatef(angle, 0, 0, 1.0f)
		player.draw
		glPopMatrix
		
		//without background, motion is not visible
		// a green grid is nice and retro
		glColor3f(0,1,0)
		renderGrid(10000)
	}
}

class Player(nx:Float,ny:Float,nz:Float){
	var x:Float = nx
	var y:Float = ny
	var z:Float = nz
	
	def applyPos {
	    glTranslatef(x,y,z)
	}

	def draw = {
		glColor3f(1, 0, 0)
		glBegin(GL_TRIANGLE_FAN)
		glNormal3d( 0, 0, 1);	glVertex3d(0,0,0.5)
		glNormal3d(-1,-1, 1);	glVertex2d(-0.5, -0.5)
		glNormal3d( 1,-1, 1);	glVertex2d(0.5, -0.5)
		glNormal3d( 1, 1, 1);	glVertex2d(0.5, 0.5)
		glNormal3d(-1, 1, 1);	glVertex2d(-0.5, 0.5)
		glNormal3d(-1,-1, 1);	glVertex2d(-0.5, -0.5)
		glEnd
	}
}

