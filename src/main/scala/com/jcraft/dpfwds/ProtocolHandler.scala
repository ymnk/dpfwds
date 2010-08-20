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
