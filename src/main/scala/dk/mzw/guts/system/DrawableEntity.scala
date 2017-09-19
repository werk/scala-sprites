package dk.mzw.guts.system

import dk.mzw.scalasprites.SpriteCanvas.Display

trait DrawableEntity extends Entity {

    def onDraw(display : Display) : Unit

}
