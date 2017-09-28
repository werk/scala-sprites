package dk.mzw.guts.entities

import dk.mzw.guts.Guts
import dk.mzw.guts.entities.GutsWorldEntity.Unspawn
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Image}

class PelletEntity(
    val world : WorldEntity,
    val self : Self,
    val position : Vector2d,
    val angle : Double,
    val speed : Double,
    val image : Image
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with CollidingEntity with HittableEntity {

    val velocity = Vector2d(Math.cos(angle), Math.sin(angle))
    velocity.multiply(speed)

    val size = Vector2d(0.2, 0.2)
    var lifeTime = 0.4 + Math.random() * 1.2
    val born = Guts.secondsElapsed()

    val collision = Collision()

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        move(world, position, size, velocity, delta, collision)

        if(Guts.secondsElapsed() - born > lifeTime || collision.hitX || collision.hitY) {
            sendMessageTo(world, Unspawn(self))
        }
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(
            image = image,
            x = position.x,
            y = position.y,
            height = 0.1,
            angle = velocity.angle - Math.PI * 0.5,
            blending = Blending.top
        )
    }

}
