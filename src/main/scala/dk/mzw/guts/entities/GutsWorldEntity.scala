package dk.mzw.guts.entities

import dk.mzw.guts.Sprites
import dk.mzw.guts.entities.GutsWorldEntity._
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.guts.system.{Vector2d, WorldEntity}
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Image}

class GutsWorldEntity(self : Self, sprites : Sprites) extends WorldEntity(self, 20) {

    val world = this

    override def internalUpdate(boundingBox : SpriteCanvas.BoundingBox, delta : Double) : Unit = {
        if(Math.random() < 0.3 * delta) {
            var r = Math.random()
            val v =
                if(r < 0.25) Vector2d(boundingBox.x1 - 5, Math.random() * boundingBox.height)
                else if(r < 0.50) Vector2d(boundingBox.x2 + 5, Math.random() * boundingBox.height)
                else if(r < 0.75) Vector2d(Math.random() * boundingBox.width, boundingBox.y1 - 5)
                else Vector2d(Math.random() * boundingBox.width, boundingBox.y2 + 5)
            r = Math.random()
            val m =
                if(r < 0.25) SpawnSkeleton(Self(), v)
                else if(r < 0.50) SpawnZombie(Self(), v)
                else if(r < 0.75) SpawnScorpion(Self(), v)
                else SpawnWolf(Self(), v)
            sendMessageTo(this, m)
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
            entities.push(new PlayerEntity(this, thatSelf, position, sprites.topManAnimation, sprites.topManShootingAnimation))
        case SpawnSkeleton(thatSelf, position) =>
            entities.push(new SkeletonEntity(this, thatSelf, position, 4, 100, sprites.skeleton))
        case SpawnZombie(thatSelf, position) =>
            entities.push(new SkeletonEntity(this, thatSelf, position, 2, 100, sprites.zombie))
        case SpawnScorpion(thatSelf, position) =>
            entities.push(new SkeletonEntity(this, thatSelf, position, 3, 50, sprites.scorpion))
        case SpawnWolf(thatSelf, position) =>
            entities.push(new SkeletonEntity(this, thatSelf, position, 5, 200, sprites.wolf))
        case SpawnWall(thatSelf, position) =>
            entities.push(new WallEntity(this, thatSelf, position, sprites.wall))
        case SpawnBarrel(thatSelf, position) =>
            entities.push(new BarrelEntity(this, thatSelf, position, sprites.barrel))
        case SpawnFlame(thatSelf, position, angle, speed) =>
            entities.push(new FlameEntity(this, thatSelf, position, angle, speed, sprites.flameRedImage, sprites.flameBrightImage))
        case SpawnPellet(thatSelf, position, angle, speed) =>
            entities.push(new PelletEntity(this, thatSelf, position, angle, speed, sprites.pelletImage))
        case SpawnCorps(thatSelf, position, angle, height, image) =>
            entities.push(new CorpsEntity(this, thatSelf, position, angle, height, image))
    }

}

object GutsWorldEntity {
    case class Unspawn(self : Self) extends Message
    case class SpawnBunny(self : Self, position : Vector2d) extends Message
    case class SpawnSkeleton(self : Self, position : Vector2d) extends Message
    case class SpawnZombie(self : Self, position : Vector2d) extends Message
    case class SpawnScorpion(self : Self, position : Vector2d) extends Message
    case class SpawnWolf(self : Self, position : Vector2d) extends Message
    case class SpawnWall(self : Self, position : Vector2d) extends Message
    case class SpawnBarrel(self : Self, position : Vector2d) extends Message
    case class SpawnFlame(self : Self, position : Vector2d, angle : Double, speed : Double) extends Message
    case class SpawnPellet(self : Self, position : Vector2d, angle : Double, speed : Double) extends Message
    case class SpawnCorps(self : Self, position : Vector2d, angle : Double, height : Double, image : Image) extends Message
}
