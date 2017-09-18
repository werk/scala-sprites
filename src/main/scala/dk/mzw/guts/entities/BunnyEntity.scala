package dk.mzw.guts.entities

import dk.mzw.guts.entities.BunnyEntity.{SetXVelocity, SetYVelocity}
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Image

class BunnyEntity(
    val self : Self,
    val position : Vector2d,
    val sprite : Image
) extends Entity with PawnEntity with CollidingEntity with ControlledEntity {

    val size = Vector2d(25, 32)
    val velocity = Vector2d(0, 0)
    val movement = Vector2d(0, 0)

    var leftArrow = false
    var rightArrow = false
    var upArrow = false
    var downArrow = false

    override def onInput(keys : Keys) : Unit = {
        if(keys(Keys.leftArrow)) {
            if (!leftArrow) {
                leftArrow = true
                rightArrow = false
                sendMessageTo(this, SetXVelocity(position.x, -100))
            }
        } else if(keys(Keys.rightArrow)) {
            if(!rightArrow) {
                rightArrow = true
                leftArrow = false
                sendMessageTo(this, SetXVelocity(position.x, 100))
            }
        } else if(leftArrow || rightArrow) {
            leftArrow = false
            rightArrow = false
            sendMessageTo(this, SetXVelocity(position.x, 0))
        }
        if(keys(Keys.downArrow)) {
            if(!downArrow) {
                downArrow = true
                upArrow = false
                sendMessageTo(this, SetYVelocity(position.y, -100))
            }
        } else if(keys(Keys.upArrow)) {
            if(!upArrow) {
                upArrow = true
                downArrow = false
                sendMessageTo(this, SetYVelocity(position.y, 100))
            }
        } else if(downArrow || upArrow) {
            upArrow = false
            downArrow = false
            sendMessageTo(this, SetYVelocity(position.y, 0))
        }
    }

    override def onMessage(message : Entity.Message) : Unit = message match {
        case SetXVelocity(x, vx) => position.x = x; velocity.x = vx
        case SetYVelocity(y, vy) => position.y = y; velocity.y = vy
    }

    override def onUpdate(entities : Seq[Entity], delta : Double) : Unit = {
        movement.set(velocity)
        movement.multiply(delta)
        move(entities, position, size, movement)
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(sprite, position.x, position.y, 32, 0)
    }
}

object BunnyEntity {
    case class SetXVelocity(x : Double, vx : Double) extends Entity.Message
    case class SetYVelocity(y : Double, vy : Double) extends Entity.Message
}