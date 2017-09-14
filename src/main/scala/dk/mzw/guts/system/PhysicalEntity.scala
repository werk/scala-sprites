package dk.mzw.guts.system

import dk.mzw.guts.system.HittableEntity.Hit
import dk.mzw.guts.system.PhysicalEntity.{Collision, Movement}

/** An entity that gets blocked by other solid entities */
trait PhysicalEntity extends PawnEntity {

    def checkMovement(direction : Vector2d) : Movement = {
        null // TODO
    }

    def move(direction : Vector2d) : Movement = {
        val movement = checkMovement(direction)
        position.set(movement.position)
        sendMovementHitMessage(direction, movement.firstCollision)
        sendMovementHitMessage(direction, movement.secondCollision)
        movement
    }

    def sendMovementHitMessage(direction : Vector2d, collision : Option[Collision]) : Unit = collision match {
        case Some(Collision(point, that : HittableEntity)) =>
            that.messageFrom(this.self, Hit(point, direction, self));
        case _ =>
    }

}

object PhysicalEntity {

    case class Movement(position : Vector2d, firstCollision : Option[Collision], secondCollision : Option[Collision])

    case class Collision(point : Vector2d, that : Entity)

}
