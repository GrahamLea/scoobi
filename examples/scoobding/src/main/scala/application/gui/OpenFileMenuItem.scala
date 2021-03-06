/**
 * Copyright 2011,2012 National ICT Australia Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gui

import swing._
import javax.swing.KeyStroke
import java.awt.event.{ActionEvent, KeyEvent}
import Images._
import java.io.File
import reactive._
import swing.FileChooser.Result
import java.awt.Component
import reactive.EventStreamSourceProxy

case class OpenFileMenuItem(start: String, label: String = "Open") extends MenuItem(label) with EventStreamSourceProxy[File] { outer =>
  override def self: Component with EventStream[File] = this.asInstanceOf[Component with EventStream[File]]

  val fileChooser = new FileChooser(new java.io.File(start))

  action = new Action(label) {
    icon = getIcon("folder-icon.png")
    mnemonic = KeyEvent.VK_O
    accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK))

    def apply() {
      fileChooser.showOpenDialog(outer) match {
        case Result.Approve => source.fire(fileChooser.selectedFile)
        case _              => ()
      }
    }
  }
}


