package dk.mzw.guts.system

import dk.mzw.guts.system.HittableEntity.Hit
import dk.mzw.guts.system.PhysicalEntity.{CollisionData, Movement}

/** An entity that gets blocked by other solid entities */
trait PhysicalEntity extends PawnEntity {

    def move(collision : Collision, deltaMovement : Vector2d) : Unit = {
        collision.move(this, deltaMovement.x, deltaMovement.y)
        //val movement = checkMovement(collision : Collision, direction)
        //position.set(movement.position)
        //sendMovementHitMessage(direction, movement.firstCollision)
        //sendMovementHitMessage(direction, movement.secondCollision)
        //movement
    }

    /*def sendMovementHitMessage(direction : Vector2d, collision : Option[CollisionData]) : Unit = collision match {
        case Some(CollisionData(point, that : HittableEntity)) =>
            that.messageFrom(this.self, Hit(point, direction, self));
        case _ =>
    }*/

}

object PhysicalEntity {

    case class Movement(position : Vector2d, firstCollision : Option[CollisionData], secondCollision : Option[CollisionData])

    case class CollisionData(point : Vector2d, that : Entity)

}
