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
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with CollidingEntity with HittableEntity with SolidEntity with ReceivingEntity {

    val velocity = Vector2d(0, 0)
    velocity.setAngle(Math.random() * Math.PI * 2, 4)
    val delayedVelocity = Vector2d(0, 0)
    delayedVelocity.set(velocity)

    val size = Vector2d(0.8, 0.8)

    val health = 100

    val born = Guts.secondsElapsed()
    var lastCollision = born

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
        val distracted = Guts.secondsElapsed() - lastCollision < 3
        if(collision.hitX || collision.hitY) {
            if(!distracted) lastCollision = Guts.secondsElapsed()
            val angle = Math.random() * Math.PI * 2
            temporary.setAngle(angle, 4)
            sendMessageTo(this, SetVelocity(position.x, position.y, temporary.x, temporary.y))
        }
        if(!distracted && Math.random() < 10 * delta) {
            val angle = world.entities.collectFirst { case e : PlayerEntity => position.angleTo(e.position) }.get
            temporary.setAngle(angle, 4)
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
            image = skeletonImage(age * 1.5),
            x = position.x,
            y = position.y,
            height = 1,
            angle = delayedVelocity.angle - Math.PI * 0.5,
            blending = Blending.top
        )
    }

}

object SkeletonEntity {
    case class SetVelocity(x : Double, y : Double, vx : Double, vy : Double) extends Message
}