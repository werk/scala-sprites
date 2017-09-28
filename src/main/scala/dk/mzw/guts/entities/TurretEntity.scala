package dk.mzw.guts.entities

import dk.mzw.guts.{Guts, Sprites}
import dk.mzw.guts.entities.GutsWorldEntity.{SpawnCorpse, SpawnFlame, SpawnSkeleton, Unspawn}
import dk.mzw.guts.entities.MortalEntity.Damage
import dk.mzw.guts.entities.SkeletonEntity.SetVelocity
import dk.mzw.guts.entities.TurretEntity.SetAngle
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.{Message, Self}
import dk.mzw.guts.system._
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Image}

class TurretEntity(
    val world : WorldEntity,
    val self : Self,
    val position : Vector2d,
    var angle : Double,
    val turretImage : Image
) extends Entity with DrawableEntity with UpdateableEntity with PawnEntity with HittableEntity with SolidEntity with ReceivingEntity with MortalEntity {

    var health = 100.0

    val size = Vector2d(0.5, 0.5)

    var fireBudget = 0.0

    override def onMessage(message: Message): Unit = message match {
        case SetAngle(a) => angle = a
        case _ => super.onMessage(message)
    }

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        fireBudget = Math.min(2, fireBudget + delta * 20)
        while(fireBudget > 1) {
            fireBudget -= 1
            var distance = 10.0
            var monster: PawnEntity = null
            var i = 0
            while (i < world.entities.length) {
                world.entities(i) match {
                    case e: SkeletonEntity if e.position.distanceTo(position) < distance =>
                        distance = e.position.distanceTo(position)
                        monster = e
                    case _ =>
                }
                i += 1
            }
            if (monster != null) {
                val a = position.angleTo(monster.position)
                sendMessageTo(this, SetAngle(a))
                val p = position.copy()
                p.add(Math.cos(a) * 0.4, Math.sin(a) * 0.4)
                sendMessageTo(world, SpawnFlame(Self(), p, a, 6))
            }
        }
    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        display.add(
            image = turretImage,
            x = position.x,
            y = position.y,
            height = 0.9,
            angle = angle + Math.PI * 0.5,
            blending = Blending.top
        )
    }

    override def onDie() = {
        sendMessageTo(world, Unspawn(self))
        sendMessageTo(world, SpawnCorpse(Self(), position, angle, 1, turretImage))
    }

}

object TurretEntity {
    case class SetAngle(angle : Double) extends Message
}