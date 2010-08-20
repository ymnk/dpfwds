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

import scala.swing._
import Swing._
import swing.event._

object DPfwdsGUI extends SimpleSwingApplication {

  var dpfwds: Option[DPfwdS] = None

  def top = new MainFrame {
    title = "Dynamic Portforwader in Scala"
    
    val textLabel = new Label("Input Text") {
      import java.awt.Font
      import javax.swing.SwingConstants._
      peer.setHorizontalAlignment(CENTER)
      peer.setFont(new Font("monospaced", Font.PLAIN, 80));
    }

    val bindAddress = new TextField("127.0.0.1", 10)
    val socksPort = new TextField("1080", 10)

    val user_host = new TextField("user@host", 10)
    val passwd = new PasswordField("", 10)

    val panel = new BoxPanel(Orientation.Vertical) {
      contents += bindAddress
      contents += socksPort
      contents += user_host
      contents += passwd
    }

    contents = new BorderPanel {
      import BorderPanel.Position._
      val button = new Button("Connect") { b =>
        reactions += {
          case ButtonClicked(_) =>
            dpfwds.map{ _dpfwds =>
              _dpfwds.terminate
              dpfwds = None
              b.text = "Connect"
	    } orElse {
              val tmp = user_host.text
              val user = tmp.substring(0, tmp.indexOf('@'))
              val host = tmp.substring(tmp.indexOf('@')+1);

              val _dpfwds = new DPfwdS(bindAddress.text,
                                      socksPort.text.toInt,
                                      user, host, sshPort=22, 
                                      passwd = Some(passwd.password.mkString))
  
              _dpfwds.start
              b.text = "Disconnect"
              dpfwds = Some(_dpfwds)
              dpfwds
	    }
        }
      }
      this.add(panel, Center)
      this.add(button, South)
    }
    
    size = (150, 250)
    
  }
}
