package dk.mzw.guts.system

import dk.mzw.guts.system.CollidingEntity.Collision

import scala.scalajs.js
import CollidingEntity._
import dk.mzw.scalasprites.Measure

/** An entity that gets blocked by other solid entities */
trait CollidingEntity extends PawnEntity {

    def canCollideWith(entity : Entity) : Boolean = true

    def move(world : WorldEntity, position : Vector2d, size : Vector2d, velocity : Vector2d, factor : Double, collision : Collision, maxMovement : Double = 100) : Unit = {

        val dx = velocity.x * factor
        val dy = velocity.y * factor

        if(Math.abs(dx) > maxMovement || Math.abs(dy) > maxMovement) {
            move(world, position, size, velocity, factor * 0.5, collision)
            move(world, position, size, velocity, factor * 0.5, collision)
            return
        }

        val r1 = position
        var hitX : Entity = null
        var hitY : Entity = null

        world.solidGrid.find(position, velocity, factor, size)

        if(dx < -moveEpsilon) {
            val x0 = r1.x - size.x * 0.5
            var x1 = x0 + dx
            val a1 = r1.y - size.y * 0.5
            val a2 = r1.y + size.y * 0.5
            var i = 0
            while(i < world.solidGrid.foundCount) {
                Measure.count("collide -dx")
                world.solidGrid.found(i) match {
                    case r2 : SolidEntity if r2 != r1 && canCollideWith(r2) =>
                        val x2 = r2.position.x + r2.size.x * 0.5
                        if(x2 <= x0) {
                            val b1 = r2.position.y - r2.size.y * 0.5
                            val b2 = r2.position.y + r2.size.y * 0.5
                            if(a1 <= b2 && b1 <= a2 && x2 >= x1) {
                                x1 = x2
                                hitX = r2
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
            while(i < world.solidGrid.foundCount) {
                Measure.count("collide +dx")
                world.solidGrid.found(i) match {
                    case r2 : SolidEntity if r2 != r1 && canCollideWith(r2) =>
                        val x2 = r2.position.x - r2.size.x * 0.5
                        if (x2 >= x0) {
                            val b1 = r2.position.y - r2.size.y * 0.5
                            val b2 = r2.position.y + r2.size.y * 0.5
                            if (a1 <= b2 && b1 <= a2 && x2 <= x1) {
                                x1 = x2
                                hitX = r2
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
            while(i < world.solidGrid.foundCount) {
                Measure.count("collide -dy")
                world.solidGrid.found(i) match {
                    case r2 : SolidEntity if r2 != r1 && canCollideWith(r2) =>
                        val y2 = r2.position.y + r2.size.y * 0.5
                        if (y2 <= y0) {
                            val b1 = r2.position.x - r2.size.x * 0.5
                            val b2 = r2.position.x + r2.size.x * 0.5
                            if (a1 <= b2 && b1 <= a2 && y2 >= y1) {
                                y1 = y2
                                hitY = r2
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
            while(i < world.solidGrid.foundCount) {
                Measure.count("collide +dy")
                world.solidGrid.found(i) match {
                    case r2 : SolidEntity if r2 != r1 && canCollideWith(r2) =>
                        val y2 = r2.position.y - r2.size.y * 0.5
                        if (y2 >= y0) {
                            val b1 = r2.position.x - r2.size.x * 0.5
                            val b2 = r2.position.x + r2.size.x * 0.5
                            if (a1 <= b2 && b1 <= a2 && y2 <= y1) {
                                y1 = y2
                                hitY = r2
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
        if(hitX != null) velocity.x = 0
        if(hitY != null) velocity.y = 0

        collision.hitX = hitX != null
        collision.hitY = hitY != null

        this match {
            case e1 : HittingEntity =>
                if(hitX != null) hitX match {
                    case e2 : HittableEntity => e1.onHit(world, e2)
                    case _ =>
                }
                if(hitY != null) hitY match {
                    case e2 : HittableEntity => e1.onHit(world, e2)
                    case _ =>
                }
            case _ =>
        }

        this match {
            case e1 : HittableEntity =>
                if(hitX != null) hitX match {
                    case e2 : HittingEntity => e2.onHit(world, e1)
                    case _ =>
                }
                if(hitY != null) hitY match {
                    case e2 : HittingEntity => e2.onHit(world, e1)
                    case _ =>
                }
            case _ =>
        }

    }

}

object CollidingEntity {

    case class Collision(var hitX : Boolean = false, var hitY : Boolean = false)

    val moveEpsilon = 0.0001
    val gapEpsilon = 0.00001

}
