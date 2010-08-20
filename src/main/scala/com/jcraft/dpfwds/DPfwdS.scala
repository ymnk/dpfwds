package com.jcraft.dpfwds

import java.net.{ServerSocket, InetAddress}

class DPfwdS (val bindAddress: String = "127.0.0.1",
             val socksPort: Int = 1080,
             val user: String,
             val host: String,
             val sshPort: Int = 22,
             val passwd: Option[String] ) extends Thread {

  var running = true
  val session = JSchSession(user, host, 22, passwd = passwd)
  var ss: Option[ServerSocket] = None

  override def run = {
    val baddress = InetAddress.getByName(bindAddress)
    ss = Some(new ServerSocket(socksPort, 0, baddress))

    while(running){
      try{
        ss.map(_.accept).map{
          socket => ProtocolHandler(socket){ session.forward }
        }
      }
      catch {
       case e if e eq IOHandlerException => println(e)
       case e => println(e)
      }
    }
  }

  def terminate = {
    running = false
    ss.foreach(_.close)
    session.disconnect
  }
}
