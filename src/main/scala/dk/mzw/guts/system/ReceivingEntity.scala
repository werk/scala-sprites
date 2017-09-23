package dk.mzw.guts.system

import dk.mzw.guts.system.Entity.Message

import scala.scalajs.js

trait ReceivingEntity extends Entity {

    var internalMessageQueue = js.Array[Message]()

    def onMessage(message : Message) : Unit

}
