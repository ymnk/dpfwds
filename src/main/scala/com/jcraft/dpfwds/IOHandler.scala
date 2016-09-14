/*
Copyright (c) 2010 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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

  // reading ipv6.address
  protected def readIPV6Address: String = {
    val i = read(buf, 0, 16)
    val tmp = new Array[Byte](16)
    System.arraycopy(buf, 0, tmp, 0, 16)
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
