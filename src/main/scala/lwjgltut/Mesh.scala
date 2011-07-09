package lwjgltut.framework

import lwjgltut.Framework._
import org.lwjgl.opengl.{Display, DisplayMode, ContextAttribs, PixelFormat}
import org.lwjgl.opengl.{GL11, GL15, GL20, GL30, GL32}
import org.lwjgl.BufferUtils
import GL11._
import GL15._
import GL20._
import GL30._
import GL32._

class RenderCmd(isIndexed: Boolean, primType: Int, 
                start: Int, elemCount: Int, indexDataType: Int, primRestart: Option[Int])

class IndexedRenderCmd(val primType: Int, val primRestart: Option[Int], val indexData: IndexData)


class PrimitiveType(glType: Int, name: String)

abstract class AttribType(val normalized: Boolean, val glType: Int, val numBytes: Int) {
  def parse(list: Array[String]) : DataHolder
}

abstract class DataHolder(val dataLength: Int) {
  def writeToBuffer(buffer: Int, offset: Int) : Unit
}

class FloatDataHolder(dataArray: Array[Float]) extends DataHolder(dataArray.length) {
  def writeToBuffer(buffer: Int, offset: Int) {
    val buf = array2Buffer(dataArray)
    glBufferSubData(buffer, offset, buf)
  }
}

class ShortDataHolder(dataArray: Array[Short]) extends DataHolder(dataArray.length) {
  def writeToBuffer(buffer: Int, offset: Int) {
    val buf = array2Buffer(dataArray)
    glBufferSubData(buffer, offset, buf)
  }
}

class Attribute(attType: String, index: Int, size: Int, isIntegral: Boolean, 
                data: DataHolder, attType2: AttribType) {

  def numElements = data.dataLength / size

  def byteSize = data.dataLength * attType2.numBytes

  def fillBoundBufferObject(offset: Int) {
    data.writeToBuffer(GL_ARRAY_BUFFER, offset)
  }
  def setupAttributeArray(offset: Int) {
    glEnableVertexAttribArray(index)
    if(isIntegral) {

    }else {
      glVertexAttribPointer(index, size, attType2.glType, attType2.normalized, 0, offset)
    }
  }
}

class FloatAttribute extends AttribType(false, GL_FLOAT, 4) {
  def parse(list: Array[String]) = { 
    val arr = list map(_ toFloat)
    new FloatDataHolder(arr)
  }
}

class ShortAttribute extends AttribType(false, GL_UNSIGNED_SHORT, 2) {
  def parse(list: Array[String]) = {
    val arr = list.map(_.toShort)
    new ShortDataHolder(arr)
  }
}

class IndexData(val data: DataHolder, val attType: AttribType) {
  
  def byteSize = data.dataLength * attType.numBytes

  def fillBoundBufferObject(offset: Int) {
    data.writeToBuffer(GL_ARRAY_BUFFER, offset)
  }
}

class MeshData(vao: Int, attribArraysBuffer: Int, indexBuffer: Int, renderCmds: Array[RenderCmd]){
  override def toString = "MeshData cmds:[%s] vao: %s buffer %s".format(renderCmds.mkString(","), 
                                                                        vao, attribArraysBuffer)
}

class Mesh(meshData: MeshData) {
  override def toString = "Mesh: [%s]".format(meshData)
}


object Mesh {

  private val attribTypes = Map("float" -> new FloatAttribute,
                                "ushort" -> new ShortAttribute)

  private val primitiveTypes = Map( "triangles" -> GL_TRIANGLES,
                                    "tri-strip" -> GL_TRIANGLE_STRIP,
                                    "tri-fan" -> GL_TRIANGLE_FAN,
                                    "lines" -> GL_LINES,
                                    "line-strip" -> GL_LINE_STRIP,
                                    "line-loop" -> GL_LINE_LOOP,
                                    "points" -> GL_POINTS)

  def getType(sType: String) = primitiveTypes(sType)

  def getAttributeType(sType: String) = attribTypes(sType)

  def load(fileName: String) = {
    val elem = scala.xml.XML.loadFile(fileName)
    val attributeElems = elem \ "attribute"
    val attributes = for (a <- attributeElems) yield {
      val atype = (a \ "@type").text
      val index = (a \ "@index").text.toInt
      val size = (a \ "@size").text.toInt
      val integral = if ((a \ "@integral").text == "true") true else false
      val inner = a.text.trim
      val attribType = getAttributeType(atype)
      val data = attribType.parse(inner.split("\\s+"))
      val attribute = new Attribute(atype, index, size, integral, data, attribType)
      attribute
    }

    val indices = elem \ "indices"
    val indexedRenderCmds = for (i <- indices) yield {
      val sType = (i \ "@type").text
      val sCmd = (i \ "@cmd").text
      val primType = getType(sCmd)
      val restart = (i \ "@prim-restart").text match {
        case "" => None
        case s => {
          val restartIdx = s.toInt
          if (restartIdx >= 0) Some(restartIdx)
          else throw new Exception("Restart must be not negative")
        }
      }
      val attType = getAttributeType(sType)
      val inner = i.text.trim
      val data = attType parse(inner.split("\\s+"))
      val indexData = new IndexData(data, attType)
      
      val cmd = new IndexedRenderCmd(primType, restart, indexData)
      cmd
    }

    val valid = attributes.map(_.numElements).toSet.size == 1
    if (!valid) throw new Exception("Some of the attribute arrays have different element counts.")

    var attribBufferSize = 0
    val startLocations = for (attrib <- attributes) yield {
      if (attribBufferSize % 16 != 0) {
        attribBufferSize += (16 - attribBufferSize % 16)
      }
      val startLoc = attribBufferSize
      attribBufferSize +=  attrib.byteSize
      startLoc
    }
    
    val vao = glGenVertexArrays
    glBindVertexArray(vao)
    
    val attribArraysBuffer = glGenBuffers
    glBindBuffer(GL_ARRAY_BUFFER, attribArraysBuffer)
    //HACK: need to create a whole buffer
    val tmpBuffer = BufferUtils.createByteBuffer(attribBufferSize)
    tmpBuffer.flip
    glBufferData(GL_ARRAY_BUFFER, tmpBuffer, GL_STATIC_DRAW)

    val zipped = attributes zip startLocations
    for((att, loc) <- zipped) {
      att.fillBoundBufferObject(loc)
      att.setupAttributeArray(loc)
    }

    glBindVertexArray(0)

    var indexBufferSize = 0
    val indexStartLocations = for (index <- indexedRenderCmds) yield {
      if (indexBufferSize % 16 != 0) {
        indexBufferSize += (16 - indexBufferSize % 16)
      }
      val startLoc = indexBufferSize
      attribBufferSize += index.indexData.byteSize
      startLoc
    }

    val zipped2 = indexedRenderCmds zip indexStartLocations

    glBindVertexArray(vao)
    val indexBuffer = glGenBuffers
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
    //HACK: creating an empty buffer
    val tmpBuffer2 = BufferUtils.createByteBuffer(indexBufferSize)
    tmpBuffer2.flip
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, tmpBuffer2, GL_STATIC_DRAW)

    val renderCmds = for((cmd, loc) <- zipped2) yield {
      cmd.indexData.fillBoundBufferObject(loc)
      new RenderCmd(true, cmd.primType, loc, cmd.indexData.data.dataLength, cmd.indexData.attType.glType, cmd.primRestart)
    }


    val md = new MeshData(vao, attribArraysBuffer, indexBuffer, renderCmds.toArray)
    val m = new Mesh(md)
    m
  }
}

object MeshTest {
  def main(args: Array[String]) {
    testLoad("data/tut7/UnitCone.xml")
  }

  def testLoad(fileName: String) {
    try {
      Display.setTitle("test")
      Display.setDisplayMode(new DisplayMode(500, 500))
      //Display.setFullscreen(true)
      Display.create
      val m = Mesh.load(fileName)
    } finally {
      Display.destroy
    }
  }
}
