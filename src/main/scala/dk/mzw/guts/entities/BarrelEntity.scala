package dk.mzw.guts.entities

import dk.mzw.guts.entities.GutsWorldEntity.{SpawnFlame, Unspawn}
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Image

class BarrelEntity(
    val self : Self,
    val position : Vector2d,
    val sprite : Image
) extends Entity with DrawableEntity with HittingEntity {

    val size = Vector2d(20, 20)

    override def onHit(world : WorldEntity, that : HittableEntity) = if(that.isInstanceOf[FlameEntity]) {
        for(_ <- 1 to 20) {
            val angle = Math.random() * Math.PI * 2
            val speed = Math.random() * 20 + 20
            sendMessageTo(world, SpawnFlame(Self(Math.random().toString, Entity.localClientId), position.copy(), angle, speed))
        }
        sendMessageTo(world, Unspawn(self))
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(sprite, position.x, position.y, 20, 0)
    }
}
