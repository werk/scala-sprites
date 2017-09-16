package dk.mzw.guts.system

import dk.mzw.scalasprites.SpriteCanvas.{Display, Loader}

class GameWorld(loader : Loader, entities : Seq[Entity]) {

    def update(delta : Double) : Unit = {
        entities.foreach { entity =>
            entity.internalMessageQueue.foreach { message =>
                entity.onMessage(message)
            }
            entity.internalMessageQueue = Nil
        }
        val collision = new Collision(entities)
        entities.foreach(_.onUpdate(collision, delta))
    }

    private val clearColor = (0.3, 0.3, 0.3, 1.0)

    def draw(display : Display, centerX : Double, centerY : Double) : Unit = {
        entities.foreach(_.onDraw(display))
        display.draw(clearColor, 600, centerX, centerY)
    }

}
