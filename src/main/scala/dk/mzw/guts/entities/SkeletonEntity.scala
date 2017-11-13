package dk.mzw.guts.entities

import dk.mzw.guts.{Guts, Sprites}
import dk.mzw.guts.entities.GutsWorldEntity.{SpawnCorpse, SpawnSkeleton, Unspawn}
import dk.mzw.guts.entities.MortalEntity.Damage
import dk.mzw.guts.entities.SkeletonEntity.SetVelocity
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.guts.system._
import dk.mzw.scalasprites.{Measure, SpriteCanvas}
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Image}

class SkeletonEntity(
    val world : WorldEntity,
    val self : Self,
    val position : Vector2d,
    val speed : Double,
    var health : Double,
    val skeletonImage : Double => Image
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with CollidingEntity with HittableEntity with HittingEntity with SolidEntity with ReceivingEntity with MortalEntity {

    val velocity = Vector2d(0, 0)
    velocity.setAngle(Math.random() * Math.PI * 2, speed)
    val delayedVelocity = Vector2d(0, 0)
    delayedVelocity.set(velocity)

    val size = Vector2d(0.8, 0.8)

    val born = Guts.secondsElapsed()
    var lastCollision = born

    val collision = Collision()

    override def onMessage(message : Entity.Message) : Unit = message match {
        case SetVelocity(x, y, vx, vy) =>
            position.set(x, y)
            velocity.set(vx, vy)
        case m => super.onMessage(m)
    }

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        move(world, position, size, velocity, delta, collision)
        if(collision.hitX) velocity.y *= 0.2
        if(collision.hitY) velocity.x *= 0.2
        val distracted = Guts.secondsElapsed() - lastCollision < 3
        if(collision.hitX || collision.hitY) {
            if(!distracted) lastCollision = Guts.secondsElapsed()
            val angle = Math.random() * Math.PI * 2
            temporary.setAngle(angle, speed)
            sendMessageTo(this, SetVelocity(position.x, position.y, temporary.x, temporary.y))
        }
        if(!distracted && Math.random() < 10 * delta) {
            world.entities.collectFirst { case e : PlayerEntity => position.angleTo(e.position) }.foreach { angle =>
                temporary.setAngle(angle, speed)
                sendMessageTo(this, SetVelocity(position.x, position.y, temporary.x, temporary.y))
            }
        }
        delayedVelocity.delay(velocity, 5, 5, delta)
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

    override def onHit(world : WorldEntity, that : HittableEntity) : Unit = that match {
        case _ : FlameEntity => sendMessageTo(this, Damage(0.1))
        case _ : PelletEntity => sendMessageTo(this, Damage(1))
        case e : PlayerEntity => sendMessageTo(e, Damage(1))
        case _ =>
    }

    override def onDie() = {
        sendMessageTo(world, Unspawn(self))
        val age = Guts.secondsElapsed() - born
        sendMessageTo(world, SpawnCorpse(Self(), position, velocity.angle, 1, skeletonImage(age * 1.5)))
    }

}

object SkeletonEntity {
    case class SetVelocity(x : Double, y : Double, vx : Double, vy : Double) extends Message
}