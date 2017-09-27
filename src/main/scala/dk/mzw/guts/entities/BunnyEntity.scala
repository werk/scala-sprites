package dk.mzw.guts.entities

import dk.mzw.guts.entities.BunnyEntity.{SetXVelocity, SetYVelocity}
import dk.mzw.guts.entities.GutsWorldEntity.SpawnFlame
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Image

class BunnyEntity(
    val self : Self,
    val position : Vector2d,
    val sprite : Image
) extends Entity
    with PawnEntity
    with CollidingEntity
    with ControlledEntity
    with ReceivingEntity
    with UpdateableEntity
    with DrawableEntity {

    val speed = 4
    val size = Vector2d(0.8, 0.8)
    val velocity = Vector2d(0, 0)
    val collision = Collision()

    var leftArrow = false
    var rightArrow = false
    var upArrow = false
    var downArrow = false

    var space = false

    override def onInput(world : WorldEntity, keys : Keys) : Unit = {
        space = keys(Keys.space)

        if(keys(Keys.leftArrow)) {
            if (!leftArrow) {
                leftArrow = true
                rightArrow = false
                sendMessageTo(this, SetXVelocity(position.x, -speed))
            }
        } else if(keys(Keys.rightArrow)) {
            if(!rightArrow) {
                rightArrow = true
                leftArrow = false
                sendMessageTo(this, SetXVelocity(position.x, speed))
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
                sendMessageTo(this, SetYVelocity(position.y, -speed))
            }
        } else if(keys(Keys.upArrow)) {
            if(!upArrow) {
                upArrow = true
                downArrow = false
                sendMessageTo(this, SetYVelocity(position.y, speed))
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

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        if(space) {
            val shotCount = Math.round(delta * 100).toInt
            for(_ <- 0 until shotCount) {
                val p = position.copy()
                val a = velocity.angle
                val s = Math.max(velocity.magnitude * 2, speed * 2.5)
                sendMessageTo(world, SpawnFlame(Self("flame-" + Math.random(), Entity.localClientId), p, a, s))
            }
        }

        temporary.set(velocity)
        move(world, position, size, temporary, delta, collision)
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(sprite, position.x, position.y, 0.8, 0)
    }
}

object BunnyEntity {
    case class SetXVelocity(x : Double, vx : Double) extends Entity.Message
    case class SetYVelocity(y : Double, vy : Double) extends Entity.Message
}