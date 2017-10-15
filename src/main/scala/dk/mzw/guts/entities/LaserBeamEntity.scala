package dk.mzw.guts.entities

import dk.mzw.guts.Guts
import dk.mzw.guts.entities.GutsWorldEntity.Unspawn
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.{Blending, CustomShader, Image}

class LaserBeamEntity(
    val world : WorldEntity,
    val self : Self,
    val shooter : PlayerEntity,
    val image : CustomShader
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with CollidingEntity with HittableEntity {

    var lifeTime = 2
    val born = Guts.secondsElapsed()
    val position = Vector2d(0, 0)
    val size = Vector2d(0.1, 0.1)
    val velocity = Vector2d(0, 0)
    val center = Vector2d(0, 0)
    var length = 1d

    val collision = Collision()

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        position.set(shooter.position)
        velocity.set(Math.cos(shooter.angle) * 1000, Math.sin(shooter.angle) * 1000)
        move(world, position, size, velocity, delta, collision)

        center.set(position)
        center.sub(shooter.position)
        length = center.magnitude
        center.multiply(0.5)
        center.add(shooter.position)

        if(Guts.secondsElapsed() - born > lifeTime) {
            sendMessageTo(world, Unspawn(self))
        }
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(
            image = image,
            x = center.x,
            y = center.y,
            width = length,
            height = 0.3,
            angle = shooter.angle,
            blending = Blending.additive
        )
    }

}
