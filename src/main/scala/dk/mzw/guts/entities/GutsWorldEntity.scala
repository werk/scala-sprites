package dk.mzw.guts.entities

import dk.mzw.guts.Sprites
import dk.mzw.guts.entities.GutsWorldEntity._
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.guts.system.{Vector2d, WorldEntity}
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Blending

class GutsWorldEntity(self : Self, sprites : Sprites) extends WorldEntity(self) {

    override def internalDraw(display : SpriteCanvas.Display, centerX : Double, centerY : Double) : Unit = {
        for(x <- (-10) to 10; y <- (-10) to 10) {
            display.add(
                image = sprites.ground,
                x = 200 * x,
                y = 200 * y,
                height = 200,
                angle = 0,
                blending = Blending.top
            )
        }
        super.internalDraw(display, centerX, centerY)
    }

    override def onMessage(message : Message) : Unit = message match {
        case Unspawn(thatSelf) =>
            var i = 0
            while(i < entities.length) {
                if(entities(i).self == thatSelf) {
                    entities.splice(i, 1)
                }
                i += 1
            }
        case SpawnBunny(thatSelf, position) =>
            entities.push(new BunnyEntity(thatSelf, position, sprites.bunny))
        case SpawnWall(thatSelf, position) =>
            entities.push(new WallEntity(thatSelf, position, sprites.wall))
        case SpawnFloor(thatSelf, position) =>
            entities.push(new FloorEntity(thatSelf, position, sprites.floor))
        case SpawnFlame(thatSelf, position, angle, speed) =>
            entities.push(new FlameEntity(thatSelf, position, angle, speed, sprites.flameRedImage, sprites.flameBrightImage))
    }

}

object GutsWorldEntity {
    case class Unspawn(self : Self) extends Message
    case class SpawnBunny(self : Self, position : Vector2d) extends Message
    case class SpawnWall(self : Self, position : Vector2d) extends Message
    case class SpawnFloor(self : Self, position : Vector2d) extends Message
    case class SpawnFlame(self : Self, position : Vector2d, angle : Double, speed : Double) extends Message
}
