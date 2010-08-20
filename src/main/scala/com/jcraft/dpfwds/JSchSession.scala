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
