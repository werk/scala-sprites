package dk.mzw.guts.system

import dk.mzw.guts.Sprites
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.SpriteCanvas.Display

import scala.collection.mutable
import scala.scalajs.js

abstract class WorldEntity(val self : Self) extends Entity with ReceivingEntity {

    val solidEntities = js.Array[SolidEntity]()
    val entities = js.Array[Entity](this)

    val keys = new Keys()

    private def consumeMessages(entity : ReceivingEntity, messages : List[Message]) : Unit = messages match {
        case Nil =>
        case message :: rest =>
            consumeMessages(entity, rest)
            //println(message)
            entity.onMessage(message)
    }

    def internalUpdate(delta : Double) : Unit = {
        var i = 0
        while(i < entities.length) {
            entities(i) match {
                case e : ControlledEntity if e.self.clientId == Entity.localClientId =>
                    e.onInput(this, keys)
                case _ =>
            }
            i += 1
        }
        i = 0
        while(i < entities.length) {
            entities(i) match {
                case e : ReceivingEntity =>
                    if (e.internalMessageQueue.nonEmpty) {
                        //println("Messages for " + e.self + ":")
                        consumeMessages(e, e.internalMessageQueue)
                        e.internalMessageQueue = Nil
                    }
                case _ =>
            }
            i += 1
        }
        solidEntities.length = 0
        i = 0
        while(i < entities.length) {
            entities(i) match {
                case e : SolidEntity => solidEntities.push(e)
                case _ =>
            }
            i += 1
        }
        i = 0
        while(i < entities.length) {
            entities(i) match {
                case e : UpdateableEntity => e.onUpdate(this, delta)
                case _ =>
            }
            i += 1
        }
    }

    private val clearColor = (0.3, 0.3, 0.3, 1.0)

    def internalDraw(display : Display, centerX : Double, centerY : Double) : Unit = {
        var i = 0
        while(i < entities.length) {
            entities(i) match {
                case e : DrawableEntity => e.onDraw(display)
                case _ =>
            }
            i += 1
        }
        display.draw(clearColor, 600, centerX, centerY)
    }

    override def onMessage(message : Message) : Unit

}
