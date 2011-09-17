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
                start: Int, elemCount: Int, indexDataType: Int, primRestart: Option[Int]) {

  def render {

    if(isIndexed) {
      glDrawElements(primType, elemCount, indexDataType, start)
    } else {
      glDrawArrays(primType, start, elemCount)
    }
  }

}

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

class Attribute(attType: String, val index: Int, size: Int, isIntegral: Boolean, 
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
    data.writeToBuffer(GL_ELEMENT_ARRAY_BUFFER, offset)
  }
}

class MeshData(val vao: Int, val attribArraysBuffer: Int, val indexBuffer: Int, val renderCmds: Array[RenderCmd], val namedVaos: Map[String, Int]){
  override def toString = "MeshData cmds:[%s] vao: %s buffer %s".format(renderCmds.mkString(","), 
                                                                        vao, attribArraysBuffer)
}

class Mesh(meshData: MeshData) {
  override def toString = "Mesh: [%s]".format(meshData)

  def render {
    glBindVertexArray(meshData.vao)
    for(p <- meshData.renderCmds) {
      p.render
    }
    glBindVertexArray(0)
  }

  def render(vaoName: String) {
    val vao = meshData.namedVaos(vaoName)
    glBindVertexArray(vao)
    for(p <- meshData.renderCmds) {
      p.render
    }
    glBindVertexArray(0)
  }
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

    val vaoElems = elem \ "vao"

    val vaos = for (v <- vaoElems) yield {
      
      val vaoName = (v \ "@name").text.toString

      val sources = v \ "source"

      val indices = sources.map(el => (el \ "@attrib").text.toInt).toArray
      val vao = (vaoName, indices)
      vao
    }


    val arrays = elem \ "arrays"

    val arrayRenderCmds = for (a <- arrays) yield {
      val sCmd = (a \ "@cmd").text
      val primType = getType(sCmd)
      val start = (a \ "@start").text.toInt
      assert (start >= 0)
      val count = (a \ "@count").text.toInt
      assert (count >= 0)
      new RenderCmd(false, primType, start, count, 0, None)
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

    
    val everythingVao = glGenVertexArrays
    glBindVertexArray(everythingVao)

    val attribArraysBuffer = glGenBuffers
    glBindBuffer(GL_ARRAY_BUFFER, attribArraysBuffer)
    //HACK: need to create a whole buffer
    val tmpBuffer = BufferUtils.createByteBuffer(attribBufferSize)
    glBufferData(GL_ARRAY_BUFFER, tmpBuffer, GL_STATIC_DRAW)

    val zipped = attributes zip startLocations
    for((att, loc) <- zipped) {
      att.fillBoundBufferObject(loc)
      att.setupAttributeArray(loc)
    }

    val vaoPairs = for (namedVao <- vaos) yield {
      val singleVao = glGenVertexArrays
      glBindVertexArray(singleVao)
      val (name, indices) = namedVao
      for (vaoIndex <- indices) {
        val (att, loc) = zipped.find(x => { val (a,l) = x; a.index == vaoIndex }).get
        att.setupAttributeArray(loc)
      }
      (name, singleVao)
    }

    val namedVaos = vaoPairs.toMap


    glBindVertexArray(0)

    var indexBufferSize = 0
    val indexStartLocations = for (index <- indexedRenderCmds) yield {
      if (indexBufferSize % 16 != 0) {
        indexBufferSize += (16 - indexBufferSize % 16)
      }
      val startLoc = indexBufferSize
      indexBufferSize += index.indexData.byteSize
      startLoc
    }

    val zipped2 = indexedRenderCmds zip indexStartLocations

    glBindVertexArray(everythingVao)
    val indexBuffer = glGenBuffers
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
    //HACK: creating an empty buffer
    val tmpBuffer2 = BufferUtils.createByteBuffer(indexBufferSize)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, tmpBuffer2, GL_STATIC_DRAW)

    val renderCmds = for((cmd, loc) <- zipped2) yield {
      cmd.indexData.fillBoundBufferObject(loc)
      new RenderCmd(true, cmd.primType, loc, cmd.indexData.data.dataLength, cmd.indexData.attType.glType, cmd.primRestart)
    }

    val allRenderCmds = renderCmds ++ arrayRenderCmds


    val md = new MeshData(everythingVao, attribArraysBuffer, indexBuffer, allRenderCmds.toArray, namedVaos)
    val m = new Mesh(md)
    m
  }
}
