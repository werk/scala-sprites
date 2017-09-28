package dk.mzw.guts.entities

import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Image

class WallEntity(
    val world : WorldEntity,
    val self : Self,
    val position : Vector2d,
    val sprite : Image
) extends Entity with SolidEntity with DrawableEntity {

    val size = Vector2d(1, 1)

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(sprite, position.x, position.y, 1, 0)
    }
}
