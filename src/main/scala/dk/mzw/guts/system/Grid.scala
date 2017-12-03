package dk.mzw.guts.system

import scala.scalajs.js
import scala.scalajs.js.|

class Grid[T <: PawnEntity] {
    private val grid = js.Dictionary[T | js.Array[T]]()
    private val cellFactor = Vector2d(1 / 1, 1 / 1)
    private val temporary = js.Array[T]()

    var found = js.Array[T]()
    var foundCount = 0

    private val to = Vector2d(0, 0)

    def find(from : Vector2d, velocity : Vector2d, delta : Double, size : Vector2d) = {
        to.set(from)
        to.addMultiplied(velocity, delta)
        val x1 = ((from.x - size.x * 0.5) * cellFactor.x - CollidingEntity.gapEpsilon).toInt
        val x2 = ((to.x + size.x * 0.5) * cellFactor.x + CollidingEntity.gapEpsilon).toInt
        val y1 = ((from.y - size.y * 0.5) * cellFactor.y - CollidingEntity.gapEpsilon).toInt
        val y2 = ((to.y + size.y * 0.5) * cellFactor.y + CollidingEntity.gapEpsilon).toInt
        foundCount = 0
        var x = x1
        while(x <= x2) {
            var y = y1
            while(y <= y2) {
                val k = x + "," + y
                if(grid.contains(k)) {
                    val entities = grid(k) match {
                        case g : js.Array[T] => g
                        case g => temporary(0) = g.asInstanceOf[T]; temporary
                    }
                    var i = 0
                    while(i < entities.length) {
                        val e = entities(i)
                        var seen = false
                        var j = 0
                        while(j < foundCount) {
                            seen = seen || (found(j) == e)
                            j += 1
                        }
                        if(!seen) {
                            found(foundCount) = e
                            foundCount += 1
                        }
                        i += 1
                    }
                }
                y += 1
            }
            x += 1
        }
    }

    def remove(e : T) = {
        val x1 = ((e.position.x - e.size.x * 0.5 - CollidingEntity.gapEpsilon) * cellFactor.x).toInt
        val x2 = ((e.position.x + e.size.x * 0.5 + CollidingEntity.gapEpsilon) * cellFactor.x).toInt
        val y1 = ((e.position.y - e.size.y * 0.5 - CollidingEntity.gapEpsilon) * cellFactor.y).toInt
        val y2 = ((e.position.y + e.size.y * 0.5 + CollidingEntity.gapEpsilon) * cellFactor.y).toInt
        var x = x1
        while(x <= x2) {
            var y = y1
            while(y <= y2) {
                val k = x + "," + y
                if(grid.contains(k)) {
                    grid(k) match {
                        case g : js.Array[T] =>
                            var i = 0
                            while(i < g.length) {
                                if(g(i) == e) {
                                    if(g.length == 1) grid.delete(k)
                                    else g.splice(i, 1)
                                    i = g.length
                                }
                                i += 1
                            }
                        case g => grid.delete(k)
                    }
                }
                y += 1
            }
            x += 1
        }
    }

    def add(e : T) = {
        val x1 = ((e.position.x - e.size.x * 0.5 - CollidingEntity.gapEpsilon) * cellFactor.x).toInt
        val x2 = ((e.position.x + e.size.x * 0.5 + CollidingEntity.gapEpsilon) * cellFactor.x).toInt
        val y1 = ((e.position.y - e.size.y * 0.5 - CollidingEntity.gapEpsilon) * cellFactor.y).toInt
        val y2 = ((e.position.y + e.size.y * 0.5 + CollidingEntity.gapEpsilon) * cellFactor.y).toInt
        var x = x1
        while(x <= x2) {
            var y = y1
            while(y <= y2) {
                val k = x + "," + y
                if(grid.contains(k)) {
                    grid(k) match {
                        case g : js.Array[T] => g.push(e)
                        case g => grid(k) = js.Array(g.asInstanceOf[T], e)
                    }
                } else {
                    grid(k) = e
                }
                y += 1
            }
            x += 1
        }
    }

}
