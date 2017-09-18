package dk.mzw.guts.system

import dk.mzw.guts.system.Entity._
import dk.mzw.scalasprites.SpriteCanvas.Display

abstract class Entity {

    val self : Self

    var internalMessageQueue : List[Message] = Nil

    def sendMessageTo(recipient : Entity, message : Message) : Unit = {
        if(self.clientId == Entity.localClientId) {
            recipient.internalMessageQueue = message :: internalMessageQueue
            Entity.broadcastMessageTo(recipient.self, message)
        }
    }

    def onMessage(message : Message) : Unit = {}

    def onUpdate(entities : Seq[Entity], delta : Double) : Unit = {}

    def onDraw(display : Display) : Unit = {}

}

object Entity {

    val localClientId : String = Math.random().toString

    def broadcastMessageTo(recipient : Self, message : Message) : Unit = {
        // TODO
    }

    case class Self(id : String, clientId : String) {
        def log(text : String) : Unit = println(id + ": " + text)
        def fail(text : String) : Nothing = throw new RuntimeException(id + ": " + text)
    }

    abstract class Message

}