package dk.mzw.guts.entities

import dk.mzw.guts.Sprites
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Image

class CorpseEntity(
    val world : WorldEntity,
    val self : Self,
    val position : Vector2d,
    val angle : Double,
    val height : Double,
    val image : Image
) extends Entity with DrawableEntity with PawnEntity {

    val size = Vector2d(height, height) // we do not need this

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(
            image = image,
            x = position.x,
            y = position.y,
            height = height,
            angle = angle - Math.PI * 0.5,
            blending = Sprites.deathBlending
        )
    }

}
