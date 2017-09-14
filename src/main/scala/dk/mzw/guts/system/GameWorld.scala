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

    def draw(display : Display) : Unit = {
        entities.foreach(_.onDraw(display))
        display.draw(600)
    }

}
