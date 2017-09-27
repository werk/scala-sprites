package dk.mzw.guts.entities

import dk.mzw.guts.Guts
import dk.mzw.guts.entities.GutsWorldEntity.Unspawn
import dk.mzw.guts.entities.SkeletonEntity.SetVelocity
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Image}

class SkeletonEntity(
    val self : Self,
    val position : Vector2d,
    val skeletonImage : Double => Image
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with CollidingEntity with HittableEntity with ReceivingEntity {

    val velocity = Vector2d(0, 0)
    velocity.setAngle(Math.random() * Math.PI * 2, 50)
    val delayedVelocity = Vector2d(0, 0)
    delayedVelocity.set(velocity)
    val temporary = Vector2d(0, 0)

    val size = Vector2d(15, 15)

    val health = 100

    val born = Guts.secondsElapsed()

    val collision = Collision()

    override def onMessage(message : Entity.Message) : Unit = message match {
        case SetVelocity(x, y, vx, vy) =>
            position.set(x, y)
            velocity.set(vx, vy)
    }

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        move(world, position, size, velocity, delta, collision)
        if(collision.hitX) velocity.y *= 0.2
        if(collision.hitY) velocity.x *= 0.2
        if(collision.hitX || collision.hitY || Math.random() < 0.001) {
            val angle = Math.random() * Math.PI * 2
            temporary.setAngle(angle, 50)
            sendMessageTo(this, SetVelocity(position.x, position.y, temporary.x, temporary.y))
        }
        delayedVelocity.delay(velocity, 5, 5, delta)

        if(health < 0) {
            sendMessageTo(world, Unspawn(self))
        }
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        val age = Guts.secondsElapsed() - born
        display.add(
            image = skeletonImage(age),
            x = position.x,
            y = position.y,
            height = 20,
            angle = delayedVelocity.angle - Math.PI * 0.5,
            blending = Blending.top
        )
    }

}

object SkeletonEntity {
    case class SetVelocity(x : Double, y : Double, vx : Double, vy : Double) extends Message
}