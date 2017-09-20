package dk.mzw.guts.system

import dk.mzw.guts.system.CollidingEntity.Collision

import scala.collection.mutable
import scala.scalajs.js

/** An entity that gets blocked by other solid entities */
trait CollidingEntity extends PawnEntity {

    def move(entities : js.Array[SolidEntity], position : Vector2d, size : Vector2d, velocity : Vector2d, factor : Double, collision : Collision) : Unit = {
        CollidingEntity.move(entities, position, size, velocity, factor, collision : Collision)
    }

}

object CollidingEntity {

    case class Collision(var hitX : Boolean = false, var hitY : Boolean = false)

    val maxMovement = 100
    val moveEpsilon = 0.0001
    val gapEpsilon = 0.00001

    def move(entities : js.Array[SolidEntity], position : Vector2d, size : Vector2d, velocity : Vector2d, factor : Double, collision : Collision) : Unit = {

        val dx = velocity.x * factor
        val dy = velocity.y * factor

        if(Math.abs(dx) > maxMovement || Math.abs(dy) > maxMovement) {
            move(entities, position, size, velocity, factor * 0.5, collision)
            move(entities, position, size, velocity, factor * 0.5, collision)
            return
        }

        val r1 = position
        var hitX = false
        var hitY = false

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
                                hitX = true
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
                                hitX = true
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
                                hitY = true
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
                                hitY = true
                            }
                        }
                    case _ =>
                }
                i += 1
            }
            val y3 = y1 - size.y * 0.5 - gapEpsilon
            if(y3 > r1.y) r1.y = y3
        }

        // Stop on hitting a wall
        if(hitX) velocity.x = 0
        if(hitY) velocity.y = 0

        collision.hitX = hitX
        collision.hitY = hitY

    }

}
