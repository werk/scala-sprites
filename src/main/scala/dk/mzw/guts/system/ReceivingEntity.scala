package dk.mzw.guts.system

import dk.mzw.guts.system.Entity.Message

trait ReceivingEntity extends Entity {

    var internalMessageQueue : List[Message] = Nil

    def onMessage(message : Message) : Unit

}
