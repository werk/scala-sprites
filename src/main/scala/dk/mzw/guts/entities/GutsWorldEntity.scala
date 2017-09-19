package dk.mzw.guts.entities

import dk.mzw.guts.Sprites
import dk.mzw.guts.entities.GutsWorldEntity._
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.guts.system.{Vector2d, WorldEntity}

class GutsWorldEntity(self : Self, sprites : Sprites) extends WorldEntity(self) {

    override def onMessage(message : Message) : Unit = message match {
        case Unspawn(thatSelf) =>
            val i = entities.indexWhere(_.self == thatSelf)
            if(i != -1) entities.remove(i)
        case SpawnBunny(thatSelf, position) =>
            entities += new BunnyEntity(thatSelf, position, sprites.bunny)
        case SpawnWall(thatSelf, position) =>
            entities += new WallEntity(thatSelf, position, sprites.wall)
        case SpawnFloor(thatSelf, position) =>
            entities += new FloorEntity(thatSelf, position, sprites.floor)
        case SpawnFlame(thatSelf, position, angle, speed) =>
            entities += new FlameEntity(thatSelf, position, angle, speed, sprites.flameRedImage, sprites.flameBrightImage)
    }

}

object GutsWorldEntity {
    case class Unspawn(self : Self) extends Message
    case class SpawnBunny(self : Self, position : Vector2d) extends Message
    case class SpawnWall(self : Self, position : Vector2d) extends Message
    case class SpawnFloor(self : Self, position : Vector2d) extends Message
    case class SpawnFlame(self : Self, position : Vector2d, angle : Double, speed : Double) extends Message
}
