package dk.mzw.guts.system

import scala.collection.mutable
import scala.scalajs.js

/** An entity that gets blocked by other solid entities */
trait CollidingEntity extends PawnEntity {

    def move(entities : js.Array[SolidEntity], position : Vector2d, size : Vector2d, deltaMovement : Vector2d) : Unit = {
        CollidingEntity.move(entities, position, size, deltaMovement.x, deltaMovement.y)
    }

}

object CollidingEntity {

    case class Movement(position : Vector2d, firstCollision : Option[CollisionData], secondCollision : Option[CollisionData])

    case class CollisionData(point : Vector2d, that : Entity)

    val maxMovement = 100
    val moveEpsilon = 0.0001
    val gapEpsilon = 0.00001

    def move(entities : js.Array[SolidEntity], position : Vector2d, size : Vector2d, dx : Double, dy : Double) : Unit = {

        if(Math.abs(dx) > maxMovement || Math.abs(dy) > maxMovement) {
            move(entities, position, size, dx * 0.5, dy * 0.5)
            move(entities, position, size, dx * 0.5, dy * 0.5)
            return
        }

        val r1 = position

        if(dx < -moveEpsilon) {
            val x0 = r1.x - size.x * 0.5
            var x1 = x0 + dx
            val a1 = r1.y - size.y * 0.5
            val a2 = r1.y + size.y * 0.5
            var i = 0
            while(i < entities.length) {
                entities(i) match {
                    case r2 : SolidEntity if r2 != r1 =>
                        val x2 = r2.position.x + r2.size.x * 0.5
                        if(x2 <= x0) {
                            val b1 = r2.position.y - r2.size.y * 0.5
                            val b2 = r2.position.y + r2.size.y * 0.5
                            if(a1 <= b2 && b1 <= a2 && x2 >= x1) {
                                x1 = x2
                            }
                        }
                    case _ =>
                }
                i += 1
            }
            val x3 = x1 + size.x * 0.5 + gapEpsilon
            if(x3 < r1.x) r1.x = x3

        } else if(dx > moveEpsilon) {
            val x0 = r1.x + size.x * 0.5
            var x1 = x0 + dx
            val a1 = r1.y - size.y * 0.5
            val a2 = r1.y + size.y * 0.5
            var i = 0
            while(i < entities.length) {
                entities(i) match {
                    case r2 : SolidEntity if r2 != r1 =>
                        val x2 = r2.position.x - r2.size.x * 0.5
                        if (x2 >= x0) {
                            val b1 = r2.position.y - r2.size.y * 0.5
                            val b2 = r2.position.y + r2.size.y * 0.5
                            if (a1 <= b2 && b1 <= a2 && x2 <= x1) {
                                x1 = x2
                            }
                        }
                    case _ =>
                }
                i += 1
            }
            val x3 = x1 - size.x * 0.5 - gapEpsilon
            if(x3 > r1.x) r1.x = x3
        }

        if(dy < -moveEpsilon) {
            val y0 = r1.y - size.y * 0.5
            var y1 = y0 + dy
            val a1 = r1.x - size.x * 0.5
            val a2 = r1.x + size.x * 0.5
            var i = 0
            while(i < entities.length) {
                entities(i) match {
                    case r2 : SolidEntity if r2 != r1 =>
                        val y2 = r2.position.y + r2.size.y * 0.5
                        if (y2 <= y0) {
                            val b1 = r2.position.x - r2.size.x * 0.5
                            val b2 = r2.position.x + r2.size.x * 0.5
                            if (a1 <= b2 && b1 <= a2 && y2 >= y1) {
                                y1 = y2
                            }
                        }
                    case _ =>
                }
                i += 1
            }
            val y3 = y1 + size.y * 0.5 + gapEpsilon
            if(y3 < r1.y) r1.y = y3

        } else if(dy > moveEpsilon) {
            val y0 = r1.y + size.y * 0.5
            var y1 = y0 + dy
            val a1 = r1.x - size.x * 0.5
            val a2 = r1.x + size.x * 0.5
            var i = 0
            while(i < entities.length) {
                entities(i) match {
                    case r2 : SolidEntity if r2 != r1 =>
                        val y2 = r2.position.y - r2.size.y * 0.5
                        if (y2 >= y0) {
                            val b1 = r2.position.x - r2.size.x * 0.5
                            val b2 = r2.position.x + r2.size.x * 0.5
                            if (a1 <= b2 && b1 <= a2 && y2 <= y1) {
                                y1 = y2
                            }
                        }
                    case _ =>
                }
                i += 1
            }
            val y3 = y1 - size.y * 0.5 - gapEpsilon
            if(y3 > r1.y) r1.y = y3
        }

    }

}
