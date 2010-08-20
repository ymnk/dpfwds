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
