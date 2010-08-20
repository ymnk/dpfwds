package com.jcraft.dpfwds

import collection.mutable.Map
import java.io.{InputStream, OutputStream}
import com.jcraft.jsch.{JSch, Session, ChannelDirectTCPIP}

object JSchSession {

  val jsch = new JSch

  val cache = Map.empty[String, JSchSession]

  def apply(user: String, host: String, port: Int = 22, passwd: Option[String] = None ) = {
    val session=jsch.getSession(user, host, port)
    session.setConfig("StrictHostKeyChecking", "no")
    passwd.foreach{ session.setPassword(_) }
    session.connect(10000)

if(session.isConnected)
   println("connected to %s@%s:%d".format(user, host, port))

    val tmp = new JSchSession(session, passwd)
    cache += (user+"@"+host+":"+port -> tmp)
    tmp
  }
}

class JSchSession (val session: Session, val passwd: Option[String]) {
  val forward = _forward _
  private def _forward(host: String, port: Int, in: InputStream, out: OutputStream) = {

println("proxy to %s:%d".format(host, port))

    session.openChannel("direct-tcpip") match{
      case channel: ChannelDirectTCPIP =>
        channel.setInputStream(in)
        channel.setOutputStream(out)
        channel.setHost(host)
        channel.setPort(port)
        channel.connect()
    }
  }

  def disconnect = session.disconnect
}
