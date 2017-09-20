package dk.mzw.guts.entities

import dk.mzw.guts.Guts
import dk.mzw.guts.entities.GutsWorldEntity.Unspawn
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Image}

class FlameEntity(
    val self : Self,
    val position : Vector2d,
    val angle : Double,
    val speed : Double,
    val flameRedImage : Image,
    val flameBrightImage : Image
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with CollidingEntity {

    val velocityAngle = (Math.random() - 0.5) * 0.2 + angle
    val velocity = Vector2d(Math.cos(velocityAngle), Math.sin(velocityAngle))
    velocity.multiply(speed)

    val size = Vector2d(10, 10)
    val lifeTime = 0.4 + Math.random() * 1.2
    val rotationSpeed = Math.random()
    val born = Guts.secondsElapsed()

    val collision = Collision()

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        move(world.solidEntities, position, size, velocity, delta, collision)
        if(collision.hitX) velocity.y *= 0.2
        if(collision.hitY) velocity.x *= 0.2

        if(Guts.secondsElapsed() - born > lifeTime) {
            sendMessageTo(world, Unspawn(self))
        }
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        val age = Guts.secondsElapsed() - born
        display.add(
            image = flameRedImage,
            x = position.x,
            y = position.y,
            height = (0.2 + parabola(age, lifeTime)) * 600 / 40,
            angle = velocity.angle + age * rotationSpeed,
            blending = Blending.additive
        )

        if (age < 0.9) {
            display.add(
                image = flameBrightImage,
                x = position.x,
                y = position.y,
                height = (0.1 + parabola(age, lifeTime - 0.3)) * 600 / 40,
                angle = velocity.angle,
                blending = Blending.additive
            )
        }
    }

    private def parabola(x : Double, s : Double = 1) : Double = Math.max(0, (4 - 4 * (x/s)) * (x/s))

}
