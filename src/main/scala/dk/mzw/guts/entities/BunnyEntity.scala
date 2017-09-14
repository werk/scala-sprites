package dk.mzw.guts.entities

import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system.{Entity, PawnEntity, Vector2d}
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Image

class BunnyEntity(
    val self : Self,
    val position : Vector2d,
    val batmanSprite : Image
) extends Entity with PawnEntity {

    val size = Vector2d(0.20, 0.20)
    val velocity = Vector2d(0, 0)

    override def onUpdate(delta : Double) : Unit = {
        velocity.set(
            if(BunnyEntity.keys(Keys.leftArrow)) -2
            else if(BunnyEntity.keys(Keys.rightArrow)) 2
            else 0,
            if(BunnyEntity.keys(Keys.downArrow)) -2
            else if(BunnyEntity.keys(Keys.upArrow)) 2
            else 0
        )
        position.add(velocity)
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(batmanSprite, position.x, position.y, 20.0, 0)
    }
}

object BunnyEntity {
    val keys = new Keys()
}