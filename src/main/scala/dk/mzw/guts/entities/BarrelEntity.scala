package dk.mzw.guts.entities

import dk.mzw.guts.entities.GutsWorldEntity.{SpawnFlame, Unspawn}
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Image

class BarrelEntity(
    val world : WorldEntity,
    val self : Self,
    val position : Vector2d,
    val sprite : Image
) extends Entity with DrawableEntity with HittingEntity with SolidEntity {

    val size = Vector2d(1, 1)
    var alive = true

    override def onHit(world : WorldEntity, that : HittableEntity) = if(alive && (that.isInstanceOf[FlameEntity] || that.isInstanceOf[PelletEntity])) {
        for(_ <- 1 to 30) {
            val angle = Math.random() * Math.PI * 2
            val speed = Math.pow(Math.random(), 3) * 20 + 1
            sendMessageTo(world, SpawnFlame(Self(Math.random().toString, Entity.localClientId), position.copy(), angle, speed))
        }
        sendMessageTo(world, Unspawn(self))
        alive = false
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(sprite, position.x, position.y, 1, 0)
    }
}
