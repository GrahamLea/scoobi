/**
 * Copyright 2011,2012 National ICT Australia Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nicta.scoobi
package impl
package util

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.binary.BinaryStreamDriver
import org.apache.hadoop.conf.Configuration
import java.io._
import core.ScoobiConfiguration
import com.thoughtworks.xstream.io.xml.StaxDriver
import com.thoughtworks.xstream.mapper.Mapper
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter
import com.thoughtworks.xstream.io.{HierarchicalStreamReader, HierarchicalStreamWriter}
import com.thoughtworks.xstream.converters.{UnmarshallingContext, MarshallingContext}

trait Serialiser {

  private val xstream = new XStream(new BinaryStreamDriver)

  xstream.omitField(classOf[Configuration],           "classLoader")
  xstream.omitField(classOf[Configuration],           "CACHE_CLASSES")
  xstream.omitField(classOf[ScoobiConfiguration],     "sc")
  xstream.omitField(classOf[ScoobiConfigurationImpl], "classLoader")
  val bridgeStoreIteratorClass = getClass.getClassLoader.loadClass("com.nicta.scoobi.impl.mapreducer.BridgeStoreIterator")
  xstream.omitField(bridgeStoreIteratorClass,  "sc")
  xstream.omitField(bridgeStoreIteratorClass,  "readers")
  xstream.omitField(bridgeStoreIteratorClass,  "remainingReaders")
  xstream.alias("list", classOf[::[_]])
  xstream.registerConverter(new ListConverter(xstream.getMapper))

  def serialise(obj: Any, out: OutputStream) = synchronized {
    try { xstream.toXML(obj, out) }
    finally { out.close()  }
  }

  def deserialise(in: InputStream) = synchronized {
    xstream.fromXML(in)
  }

  def toByteArray(obj: Any) = {
    val out = new ByteArrayOutputStream
    serialise(obj, out)
    out.toByteArray
  }

  def fromByteArray(in: Array[Byte]) =
    deserialise(new ByteArrayInputStream(in))


  class ListConverter(_mapper : Mapper)  extends AbstractCollectionConverter(_mapper) {
    /** Helper method to use x.getClass
      *
      * See: http://scalide.blogspot.com/2009/06/getanyclass-tip.html
      */
    def getAnyClass(x: Any) = x.asInstanceOf[AnyRef].getClass

    def canConvert( clazz: Class[_]) = {
      classOf[::[_]] == clazz
    }

    def marshal( value: Any, writer: HierarchicalStreamWriter, context: MarshallingContext) {
      val list = value.asInstanceOf[List[_]]
      for ( item <- list ) {
        writeItem(item, context, writer)
      }
    }

    def unmarshal( reader: HierarchicalStreamReader, context: UnmarshallingContext ) = {
      var list : List[_] = Nil
      while (reader.hasMoreChildren) {
        reader.moveDown()
        val item = readItem(reader, context, list)
        list = list ::: List(item) // be sure to build the list in the same order
        reader.moveUp()
      }
      list
    }
  }
}
object Serialiser extends Serialiser
