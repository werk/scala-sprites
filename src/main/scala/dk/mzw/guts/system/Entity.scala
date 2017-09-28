package dk.mzw.guts.system

import dk.mzw.guts.system.Entity._

abstract class Entity {

    val self : Self
    val world : WorldEntity

    def sendMessageTo(recipient : ReceivingEntity, message : Message) : Unit = {
        if(self.clientId == Entity.localClientId) {
            recipient.internalMessageQueue.push(message)
            Entity.broadcastMessageTo(recipient.self, message)
        }
    }

}

object Entity {

    val localClientId : String = Math.random().toString

    def broadcastMessageTo(recipient : Self, message : Message) : Unit = {
        // TODO
    }

    case class Self(id : String = Math.random().toString, clientId : String = Entity.localClientId) {
        def log(text : String) : Unit = println(id + ": " + text)
        def fail(text : String) : Nothing = throw new RuntimeException(id + ": " + text)
    }

    abstract class Message

}