package dk.mzw.guts.system

import dk.mzw.guts.entities.SkeletonEntity
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.guts.utility.Mouse
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.Measure
import dk.mzw.scalasprites.SpriteCanvas.{BoundingBox, Display}

import scala.scalajs.js

abstract class WorldEntity(val self : Self, val screenHeight : Double) extends Entity with ReceivingEntity {

    val entities = js.Array[Entity](this)

    val solidGrid = new Grid[SolidEntity]()
    val hittableGrid = new Grid[HittableEntity]()

    val keys = new Keys()

    def internalUpdate(boundingBox : BoundingBox, mouse : Mouse, delta : Double) : Unit = {
        var i = 0
        Measure("Controlled") (while(i < entities.length) {
            entities(i) match {
                case e : ControlledEntity if e.self.clientId == Entity.localClientId =>
                    e.onInput(this, keys, mouse)
                case _ =>
            }
            i += 1
        })
        i = 0
        Measure("Receiving") (while(i < entities.length) {
            entities(i) match {
                case e : ReceivingEntity =>
                    if(e.internalMessageQueue.length != 0) {
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
        })
        i = 0
        Measure("Updateable and Hitting") (while(i < entities.length) {
            entities(i) match {
                case e : UpdateableEntity with PawnEntity =>
                    if (e.position.x > boundingBox.x1 - 10 && e.position.x < boundingBox.x2 + 10 &&
                        e.position.y > boundingBox.y1 - 10 && e.position.y < boundingBox.y2 + 10
                    ) Measure("onUpdate Pawn") {
                        internalRemoveEntityFromGrids(e)
                        e.onUpdate(this, delta)
                        internalAddEntityToGrids(e)
                    }
                case e : UpdateableEntity =>
                    Measure("onUpdate") (e.onUpdate(this, delta))
                case _ =>
            }
            entities(i) match {
                case e : HittingEntity => e.internalEmitHits(this)
                case _ =>
            }
            i += 1
        })
    }

    def internalRemoveEntityFromGrids(entity : Entity) = {
        entity match {
            case e : SolidEntity => solidGrid.remove(e)
            case _ =>
        }
        entity match {
            case e : HittableEntity => hittableGrid.remove(e)
            case _ =>
        }
    }

    def internalAddEntityToGrids(entity : Entity) = {
        entity match {
            case e : SolidEntity => solidGrid.add(e)
            case _ =>
        }
        entity match {
            case e : HittableEntity => hittableGrid.add(e)
            case _ =>
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
