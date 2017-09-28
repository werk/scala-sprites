package dk.mzw.guts.entities

import dk.mzw.guts.Sprites
import dk.mzw.guts.entities.GutsWorldEntity._
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.guts.system.{Vector2d, WorldEntity}
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Blending

class GutsWorldEntity(self : Self, sprites : Sprites) extends WorldEntity(self, 20) {

    override def internalUpdate(boundingBox : SpriteCanvas.BoundingBox, delta : Double) : Unit = {
        if(Math.random() < 0.3 * delta) {
            val r = Math.random()
            val v =
                if(r < 0.25) Vector2d(boundingBox.x1 - 5, Math.random() * boundingBox.height)
                else if(r < 0.50) Vector2d(boundingBox.x2 + 5, Math.random() * boundingBox.height)
                else if(r < 0.75) Vector2d(Math.random() * boundingBox.width, boundingBox.y1 - 5)
                else Vector2d(Math.random() * boundingBox.width, boundingBox.y2 + 5)
            sendMessageTo(this, SpawnSkeleton(Self(), v))
        }
        super.internalUpdate(boundingBox, delta)
    }

    override def internalDraw(display : SpriteCanvas.Display, centerX : Double, centerY : Double) : Unit = {
        val size = 6.3
        for(x <- (-4) to 4; y <- (-4) to 4) {
            display.add(
                image = sprites.ground,
                x = centerX + size * x - centerX % size,
                y = centerY + size * y - centerY % size,
                height = size,
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
        case SpawnSkeleton(thatSelf, position) =>
            entities.push(new SkeletonEntity(thatSelf, position, sprites.skeleton))
        case SpawnWall(thatSelf, position) =>
            entities.push(new WallEntity(thatSelf, position, sprites.wall))
        case SpawnBarrel(thatSelf, position) =>
            entities.push(new BarrelEntity(thatSelf, position, sprites.barrel))
        case SpawnFlame(thatSelf, position, angle, speed) =>
            entities.push(new FlameEntity(thatSelf, position, angle, speed, sprites.flameRedImage, sprites.flameBrightImage))
    }

}

object GutsWorldEntity {
    case class Unspawn(self : Self) extends Message
    case class SpawnBunny(self : Self, position : Vector2d) extends Message
    case class SpawnSkeleton(self : Self, position : Vector2d) extends Message
    case class SpawnWall(self : Self, position : Vector2d) extends Message
    case class SpawnBarrel(self : Self, position : Vector2d) extends Message
    case class SpawnFlame(self : Self, position : Vector2d, angle : Double, speed : Double) extends Message
}
