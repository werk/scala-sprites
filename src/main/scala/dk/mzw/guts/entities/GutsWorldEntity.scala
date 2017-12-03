package dk.mzw.guts.entities

import dk.mzw.guts.Sprites
import dk.mzw.guts.entities.GutsWorldEntity._
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.guts.system.{Entity, Vector2d, WorldEntity}
import dk.mzw.guts.utility.Mouse
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Image}

class GutsWorldEntity(self : Self, sprites : Sprites) extends WorldEntity(self, 20) {

    val world = this

    override def internalUpdate(boundingBox : SpriteCanvas.BoundingBox, mouse : Mouse, delta : Double) : Unit = {
        super.internalUpdate(boundingBox, mouse, delta)
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
                val e = entities(i)
                if(e.self == thatSelf) {
                    internalRemoveEntityFromGrids(e)
                    entities.splice(i, 1)
                }
                i += 1
            }
        case SpawnPlayer(thatSelf, position) =>
            addEntity(new PlayerEntity(this, thatSelf, position, sprites.topManAnimation, sprites.topManShootingAnimation))
        case SpawnSkeleton(thatSelf, position) =>
            addEntity(new SkeletonEntity(this, thatSelf, position, 4, 80, sprites.skeleton))
        case SpawnZombie(thatSelf, position) =>
            addEntity(new SkeletonEntity(this, thatSelf, position, 2, 120, sprites.zombie))
        case SpawnScorpion(thatSelf, position) =>
            addEntity(new SkeletonEntity(this, thatSelf, position, 5, 20, sprites.scorpion))
        case SpawnWolf(thatSelf, position) =>
            addEntity(new SkeletonEntity(this, thatSelf, position, 1.5, 200, sprites.wolf))
        case SpawnWall(thatSelf, position) =>
            addEntity(new WallEntity(this, thatSelf, position, sprites.wall))
        case SpawnFloor(thatSelf, position) =>
            addEntity(new FloorEntity(this, thatSelf, position, sprites.floor))
        case SpawnBarrel(thatSelf, position) =>
            addEntity(new BarrelEntity(this, thatSelf, position, sprites.barrel))
        case SpawnFlame(thatSelf, position, angle, speed) =>
            addEntity(new FlameEntity(this, thatSelf, position, angle, speed, sprites.flameRedImage, sprites.flameBrightImage))
        case SpawnPellet(thatSelf, position, angle, speed) =>
            addEntity(new PelletEntity(this, thatSelf, position, angle, speed, sprites.pelletImage))
        case SpawnLaserBeam(thatSelf, shooterId) =>
            entities.find(_.self.id == shooterId).foreach { shooter =>
                val player = shooter.asInstanceOf[PlayerEntity]
                addEntity(new LaserBeamEntity(this, thatSelf, player, sprites.laserBeamImage, sprites.roundFlame))
            }
        case SpawnTurret(thatSelf, position, angle) =>
            addEntity(new TurretEntity(this, thatSelf, position, angle, sprites.turret))
        case SpawnCorpse(thatSelf, position, angle, height, image) =>
            addEntity(new CorpseEntity(this, thatSelf, position, angle, height, image))
    }
    
    private def addEntity(entity : Entity) = {
        entities.push(entity)
        internalAddEntityToGrids(entity)
    }

}

object GutsWorldEntity {
    case class Unspawn(self : Self) extends Message
    case class SpawnPlayer(self : Self, position : Vector2d) extends Message
    case class SpawnSkeleton(self : Self, position : Vector2d) extends Message
    case class SpawnZombie(self : Self, position : Vector2d) extends Message
    case class SpawnScorpion(self : Self, position : Vector2d) extends Message
    case class SpawnWolf(self : Self, position : Vector2d) extends Message
    case class SpawnWall(self : Self, position : Vector2d) extends Message
    case class SpawnFloor(self : Self, position : Vector2d) extends Message
    case class SpawnBarrel(self : Self, position : Vector2d) extends Message
    case class SpawnTurret(self : Self, position : Vector2d, angle : Double) extends Message
    case class SpawnFlame(self : Self, position : Vector2d, angle : Double, speed : Double) extends Message
    case class SpawnPellet(self : Self, position : Vector2d, angle : Double, speed : Double) extends Message
    case class SpawnLaserBeam(self : Self, shooterId : String) extends Message
    case class SpawnCorpse(self : Self, position : Vector2d, angle : Double, height : Double, image : Image) extends Message
}
