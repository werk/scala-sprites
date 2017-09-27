package dk.mzw.guts.system

import dk.mzw.guts.Sprites
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.SpriteCanvas.Display

import scala.collection.mutable
import scala.scalajs.js

abstract class WorldEntity(val self : Self, val screenHeight : Double) extends Entity with ReceivingEntity {

    val solidEntities = js.Array[SolidEntity]()
    val hittableEntities = js.Array[HittableEntity]()
    val entities = js.Array[Entity](this)

    val keys = new Keys()

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
                    if(e.internalMessageQueue.length != 0) {
                        //println("Messages for " + e.self + ":")
                        var j = 0
                        while(j < e.internalMessageQueue.length) {
                            e.onMessage(e.internalMessageQueue(j))
                            j += 1
                        }
                        e.internalMessageQueue.length = 0
                    }
                case _ =>
            }
            i += 1
        }
        solidEntities.length = 0
        hittableEntities.length = 0
        i = 0
        while(i < entities.length) {
            entities(i) match {
                case e : SolidEntity => solidEntities.push(e)
                case _ =>
            }
            entities(i) match {
                case e : HittableEntity => hittableEntities.push(e)
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
            entities(i) match {
                case e : HittingEntity => e.internalEmitHits(this, hittableEntities)
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
        display.draw(clearColor, screenHeight, centerX, centerY)
    }

    override def onMessage(message : Message) : Unit

}
