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

import java.net.Socket
import java.io.{InputStream, OutputStream}

object ProtocolHandler {
  def apply(socket: Socket): ProtocolHandler = {
    socket.setTcpNoDelay(true)
    val in = socket.getInputStream
    val out = socket.getOutputStream

    in.read match{
      case 4 => 
        new Socks4ProtocolHandler(socket, in, out)
      case 5 => 
        new Socks5ProtocolHandler(socket, in, out)
      case -1 => 
        error("io error")
      case v =>
        error("unknown protocol version: "+(v&0xff))
    }
  }
}

trait ProtocolHandler { self: IOHandler =>
  val socket: Socket
  val in: InputStream
  val out: OutputStream

  def apply(f: (String, Int, InputStream, OutputStream) => Unit): Unit

  def close() = {
    in.close
    out.close
    socket.close
  } 
}

class Socks4ProtocolHandler(
  override val socket: Socket,
  override val in: InputStream,
  override val out: OutputStream) extends IOHandler with ProtocolHandler {

  def apply(f: (String, Int, InputStream, OutputStream) => Unit) = {
    val cd = read  // 1

    val port = readShort

    val host = readIPV4Address

    val name = readString

    f(host, port, in, out)

    buf(0) = 0
    buf(1) = 90
    out.write(buf, 0, 8)
    out.flush 
  }
}

class Socks5ProtocolHandler(
  override val socket: Socket,
  override val in: InputStream,
  override val out: OutputStream) extends IOHandler with ProtocolHandler {

  def apply(f: (String, Int, InputStream, OutputStream) => Unit) = {

    val methods = readBytes

    buf(0) = 5
    buf(1) = 0
    out.write(buf, 0, 2)
    out.flush

    val version = read
    val cd = read
    val rsv = read
    val atyp = read

    val host = atyp match{
      case 1 =>
        readIPV4Address
      case 3 =>
        readNString
    }

    val port = readShort

    f(host, port, in, out)

    buf(0) = 5
    buf(1) = 0
    buf(2) = 0
    buf(3) = 1
    buf(4) = 0; buf(5) = 0; buf(6) = 0; buf(7) = 0
    buf(8) = 0; buf(9) = 0

    out.write(buf, 0, 10)
    out.flush
  }
}
