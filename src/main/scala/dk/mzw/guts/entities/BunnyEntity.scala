package dk.mzw.guts.entities

import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Image

class BunnyEntity(
    val self : Self,
    val position : Vector2d,
    val sprite : Image
) extends Entity with PawnEntity with PhysicalEntity {

    val size = Vector2d(20, 20)

    override def onUpdate(collision : Collision, delta : Double) : Unit = {
        val velocity = Vector2d(0, 0)
        velocity.set(
            if(BunnyEntity.keys(Keys.leftArrow)) -100
            else if(BunnyEntity.keys(Keys.rightArrow)) 100
            else 0,
            if(BunnyEntity.keys(Keys.downArrow)) -100
            else if(BunnyEntity.keys(Keys.upArrow)) 100
            else 0
        )
        velocity.multiply(delta)
        move(collision, velocity)
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(sprite, position.x, position.y, 20.0, 0)
    }
}

object BunnyEntity {
    val keys = new Keys()
}