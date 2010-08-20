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
