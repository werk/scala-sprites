package dk.mzw.guts.entities

import dk.mzw.guts.Guts
import dk.mzw.guts.entities.GutsWorldEntity.Unspawn
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.{Blending, CustomShader, Image}

class FlameEntity(
             val world : WorldEntity,
    val self : Self,
    val position : Vector2d,
    val angle : Double,
    val speed : Double,
    val flameRedImage : Image,
    val flameBrightImage : CustomShader
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with CollidingEntity with HittableEntity {

    val velocityAngle = angle + ((Math.random() - 0.5) * (Math.random() - 0.5)) * 0.5
    val velocity = Vector2d(Math.cos(velocityAngle), Math.sin(velocityAngle))
    velocity.multiply(speed)

    val size = Vector2d(0.5, 0.5)
    var lifeTime = 0.4 + Math.random() * 1.2
    val rotationSpeed = Math.random()
    val born = Guts.secondsElapsed()

    val collision = Collision()

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        move(world, position, size, velocity, delta, collision)
        if(collision.hitX) velocity.y *= 0.2
        if(collision.hitY) velocity.x *= 0.2
        if(collision.hitX || collision.hitY) lifeTime -= (lifeTime - (Guts.secondsElapsed() - born)) * 0.5

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
            height = 0.1 + parabola(age, lifeTime),
            angle = velocity.angle + age * rotationSpeed,
            blending = Blending.additive
        )

        val brightHeight = 0.05 + parabola(age, lifeTime - 0.3)
        if(brightHeight > 0.25) {
            display.add(
                image = flameBrightImage,
                x = position.x,
                y = position.y,
                height = brightHeight,
                angle = velocity.angle,
                blending = Blending.additive
            )
        }
    }

    private def parabola(x : Double, s : Double = 1) : Double = Math.max(0, (4 - 4 * (x/s)) * (x/s))

}
