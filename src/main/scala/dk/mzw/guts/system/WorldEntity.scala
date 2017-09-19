package dk.mzw.guts.system

import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.SpriteCanvas.{Display, Loader}

class WorldEntity(val self : Self, loader : Loader, val entities : Seq[Entity]) extends Entity {

    val keys = new Keys()

    private def consumeMessages(entity : ReceivingEntity, messages : List[Message]) : Unit = messages match {
        case Nil =>
        case message :: rest =>
            consumeMessages(entity, rest)
            println(message)
            entity.onMessage(message)
    }

    def internalUpdate(delta : Double) : Unit = {
        entities.foreach {
            case e : ControlledEntity if e.self.clientId == Entity.localClientId =>
                e.onInput(this, keys)
            case _ =>
        }
        entities.foreach {
            case e : ReceivingEntity =>
                if(e.internalMessageQueue.nonEmpty) {
                    println("Messages for " + e.self + ":")
                    consumeMessages(e, e.internalMessageQueue)
                    e.internalMessageQueue = Nil
                }
            case _ =>
        }
        entities.foreach {
            case e : UpdateableEntity => e.onUpdate(this, delta)
            case _ =>
        }
    }

    private val clearColor = (0.3, 0.3, 0.3, 1.0)

    def internalDraw(display : Display, centerX : Double, centerY : Double) : Unit = {
        entities.foreach {
            case e : DrawableEntity => e.onDraw(display)
            case _ =>
        }
        display.draw(clearColor, 600, centerX, centerY)
    }

}
