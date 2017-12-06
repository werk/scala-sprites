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
    val beamEffect : Double => CustomShader,
    val impact : CustomShader
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with CollidingEntity with HittableEntity {

    var lifeTime = 2
    val born = Guts.secondsElapsed()
    var age = 0d
    val position = Vector2d(0, 0)
    val size = Vector2d(0.1, 0.1)
    val velocity = Vector2d(0, 0)
    val center = Vector2d(0, 0)
    var length = 1d

    val collision = Collision()

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        position.set(shooter.position)
        val availableDx = Math.cos(shooter.angle) * 10
        val availableDy = Math.sin(shooter.angle) * 10
        velocity.set(availableDx, availableDy)
        move(world, position, size, velocity, 1, collision, 0.1)
        val dx = position.x - shooter.position.x
        val dy = position.y - shooter.position.y
        val usedDx = dx / availableDx
        val usedDy = dy / availableDy
        if(usedDx < usedDy) position.y = shooter.position.y + availableDy * usedDx
        if(usedDx > usedDy) position.x = shooter.position.x + availableDx * usedDy


        center.set(position)
        center.sub(shooter.position)
        length = center.magnitude
        center.multiply(0.5)
        center.add(shooter.position)

        age = Guts.secondsElapsed() - born
        if(age > lifeTime) {
            sendMessageTo(world, Unspawn(self))
        }
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(
            image = beamEffect(age),
            imageX = -1,
            imageY = -1,
            imageWidth = length.toFloat,
            imageHeight = 2,
            x = center.x,
            y = center.y,
            width = length,
            height = 0.3,
            angle = shooter.angle,
            blending = Blending.additive
        )
        display.add(
            image = impact,
            x = position.x,
            y = position.y,
            width = 1,
            height = 1,
            angle = 0,
            blending = Blending.additive
        )
    }

}
