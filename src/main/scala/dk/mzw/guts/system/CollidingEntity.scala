package dk.mzw.guts.system

import dk.mzw.guts.system.HittableEntity.Hit
import dk.mzw.guts.system.CollidingEntity.{CollisionData, Movement}

/** An entity that gets blocked by other solid entities */
trait CollidingEntity extends PawnEntity {

    def move(entities : Seq[Entity], result : Vector2d, size : Vector2d, deltaMovement : Vector2d) : Unit = {
        CollidingEntity.move(entities, result, size, deltaMovement.x, deltaMovement.y)
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

object CollidingEntity {

    case class Movement(position : Vector2d, firstCollision : Option[CollisionData], secondCollision : Option[CollisionData])

    case class CollisionData(point : Vector2d, that : Entity)

    val moveEpsilon = 0.0001
    val gapEpsilon = 0.00001

    def move(entities : Seq[Entity], result : Vector2d, size : Vector2d, dx : Double, dy : Double) : Unit = {

        if(Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) {
            move(entities, result, size, dx * 0.5, dy * 0.5)
            move(entities, result, size, dx * 0.5, dy * 0.5)
            return
        }

        val r1 = result

        if(dx < -moveEpsilon) {
            val x0 = r1.x - size.x * 0.5
            var x1 = x0 + dx
            val a1 = r1.y - size.y * 0.5
            val a2 = r1.y + size.y * 0.5
            for(r2 <- entities.collect { case e : SolidEntity => e }) if(r2 != r1) {
                val x2 = r2.position.x + r2.size.x * 0.5
                if(x2 <= x0) {
                    val b1 = r2.position.y - r2.size.y * 0.5
                    val b2 = r2.position.y + r2.size.y * 0.5
                    if(a1 <= b2 && b1 <= a2 && x2 >= x1) {
                        x1 = x2
                    }
                }
            }
            val x3 = x1 + size.x * 0.5 + gapEpsilon
            if(x3 < r1.x) r1.x = x3

        } else if(dx > moveEpsilon) {
            val x0 = r1.x + size.x * 0.5
            var x1 = x0 + dx
            val a1 = r1.y - size.y * 0.5
            val a2 = r1.y + size.y * 0.5
            for(r2 <- entities.collect { case e : SolidEntity => e }) if(r2 != r1) {
                val x2 = r2.position.x - r2.size.x * 0.5
                if(x2 >= x0) {
                    val b1 = r2.position.y - r2.size.y * 0.5
                    val b2 = r2.position.y + r2.size.y * 0.5
                    if(a1 <= b2 && b1 <= a2 && x2 <= x1) {
                        x1 = x2
                    }
                }
            }
            val x3 = x1 - size.x * 0.5 - gapEpsilon
            if(x3 > r1.x) r1.x = x3
        }

        if(dy < -moveEpsilon) {
            val y0 = r1.y - size.y * 0.5
            var y1 = y0 + dy
            val a1 = r1.x - size.x * 0.5
            val a2 = r1.x + size.x * 0.5
            for(r2 <- entities.collect { case e : SolidEntity => e }) if(r2 != r1) {
                val y2 = r2.position.y + r2.size.y * 0.5
                if(y2 <= y0) {
                    val b1 = r2.position.x - r2.size.x * 0.5
                    val b2 = r2.position.x + r2.size.x * 0.5
                    if(a1 <= b2 && b1 <= a2 && y2 >= y1) {
                        y1 = y2
                    }
                }
            }
            val y3 = y1 + size.y * 0.5 + gapEpsilon
            if(y3 < r1.y) r1.y = y3

        } else if(dy > moveEpsilon) {
            val y0 = r1.y + size.y * 0.5
            var y1 = y0 + dy
            val a1 = r1.x - size.x * 0.5
            val a2 = r1.x + size.x * 0.5
            for(r2 <- entities.collect { case e : SolidEntity => e }) if(r2 != r1) {
                val y2 = r2.position.y - r2.size.y * 0.5
                if(y2 >= y0) {
                    val b1 = r2.position.x - r2.size.x * 0.5
                    val b2 = r2.position.x + r2.size.x * 0.5
                    if(a1 <= b2 && b1 <= a2 && y2 <= y1) {
                        y1 = y2
                    }
                }
            }
            val y3 = y1 - size.y * 0.5 - gapEpsilon
            if(y3 > r1.y) r1.y = y3
        }

    }

}
