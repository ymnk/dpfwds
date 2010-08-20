package com.jcraft.dpfwds

import java.net.InetAddress
import java.io.{InputStream, OutputStream}

object IOHandlerException extends Exception
trait IOHandler {

  val in: InputStream
  val out: OutputStream

  protected val buf = new Array[Byte](1024)

  protected def read(buf: Array[Byte], s: Int, l: Int): Unit = {
    var _s = s
    var _l = l
    while(_l > 0){
      in.read(buf, _s, _l) match { 
        case -1 => throw IOHandlerException
        case i =>
          _s += i
          _l -= i
      }
    }
    
  }

  protected def read: Byte = {
    in.read match {
      case -1 => throw IOHandlerException
      case c => (c&0xff).asInstanceOf[Byte] 
    }
  }

  // reading ipv4.address
  protected def readIPV4Address: String = {
    val i = read(buf, 0, 4) 
    val tmp = new Array[Byte](4)
    System.arraycopy(buf, 0, tmp, 0, 4)
    InetAddress.getByAddress(tmp).getHostAddress
  }

  // reading string, which starts with its length
  protected def readNString: String = {
    val n = read
    read(buf, 0, n)
    new String(buf, 0, n)
  }

  // reading null terminated string.
  protected def readString: String = {
    val str = scala.collection.mutable.ArrayBuffer.empty[Char]
    for(c <- Stream.continually(read).takeWhile(_ != 0)){
      str += (c%0xff).asInstanceOf[Char]
    }
    str.mkString
  }

  // reading null terminated string.
  protected def readShort: Int = {
    val i = read(buf, 0, 2) 
    ((buf(0)<<8)&0xff00) | (buf(1)&0xff)
  }

  protected def readBytes: Array[Byte] = {
    val n = read
    (0 until n).foldLeft(new Array[Byte](n)) {
        case (a, index) => 
          a(index) = read
          a
      }
  }
}
