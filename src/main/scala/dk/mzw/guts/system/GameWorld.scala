package dk.mzw.guts.system

import dk.mzw.guts.system.Entity.Message
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.SpriteCanvas.{Display, Loader}

class GameWorld(loader : Loader, entities : Seq[Entity]) {

    val keys = new Keys()

    def consumeMessages(entity : Entity, messages : List[Message]) : Unit = messages match {
        case Nil =>
        case message :: rest =>
            consumeMessages(entity, rest)
            entity.onMessage(message)
    }

    def update(delta : Double) : Unit = {
        entities.collect { case e : ControlledEntity => e }.foreach(_.onInput(keys))
        entities.foreach { entity =>
            if(entity.internalMessageQueue.nonEmpty) {
                consumeMessages(entity, entity.internalMessageQueue)
                entity.internalMessageQueue = Nil
            }
        }
        entities.foreach(_.onUpdate(entities, delta))
    }

    def draw(display : Display) : Unit = {
        entities.foreach(_.onDraw(display))
        display.draw(600)
    }

}
