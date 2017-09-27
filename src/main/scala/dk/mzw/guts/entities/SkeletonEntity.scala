package dk.mzw.guts.entities

import dk.mzw.guts.Guts
import dk.mzw.guts.entities.GutsWorldEntity.Unspawn
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Image}

class SkeletonEntity(
    val self : Self,
    val position : Vector2d,
    val skeletonImage : Double => Image
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with CollidingEntity with HittableEntity {

    val velocity = Vector2d(0, 0)
    velocity.setAngle(Math.random() * Math.PI * 2, 50)
    val delayedVelocity = Vector2d(0, 0)
    delayedVelocity.set(velocity)

    val size = Vector2d(20, 20)

    val health = 100

    val born = Guts.secondsElapsed()

    val collision = Collision()

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        move(world, position, size, velocity, delta, collision)
        if(collision.hitX) velocity.y *= 0.2
        if(collision.hitY) velocity.x *= 0.2
        if(collision.hitX || collision.hitY || Math.random() < 0.001) {
            val angle = Math.random() * Math.PI * 2
            velocity.setAngle(angle, 50)
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
