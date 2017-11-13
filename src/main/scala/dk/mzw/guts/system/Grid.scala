package dk.mzw.guts.system

import scala.scalajs.js

object Grid {
    private var grid = js.Dictionary[js.Array[PawnEntity]]()
    private val cellFactor = Vector2d(1 / 1, 1 / 1)

    var found = js.Array[PawnEntity]()
    var foundCount = 0

    private val to = Vector2d(0, 0)

    def find(from : Vector2d, velocity : Vector2d, delta : Double, size : Vector2d) = {
        to.set(from)
        to.addMultiplied(velocity, delta)
        val x1 = Math.floor((from.x - size.x * 0.5) * Grid.cellFactor.x - CollidingEntity.gapEpsilon).toInt
        val x2 = Math.ceil((to.x + size.x * 0.5) * Grid.cellFactor.x + CollidingEntity.gapEpsilon).toInt
        val y1 = Math.floor((from.y - size.y * 0.5) * Grid.cellFactor.y - CollidingEntity.gapEpsilon).toInt
        val y2 = Math.ceil((to.y + size.y * 0.5) * Grid.cellFactor.y + CollidingEntity.gapEpsilon).toInt
        foundCount = 0
        var x = x1
        while(x <= x2) {
            var y = y1
            while(y <= y2) {
                val k = x + "," + y
                if(Grid.grid.contains(k)) {
                    val entities = Grid.grid(k)
                    var i = 0
                    while(i < entities.length) {
                        found(foundCount) = entities(i)
                        foundCount += 1
                        i += 1
                    }
                }
                y += 1
            }
            x += 1
        }
    }

    def rebuild(entities : js.Array[SolidEntity]) = {
        grid = js.Dictionary[js.Array[PawnEntity]]()
        var i = 0
        while(i < entities.length) {
            val e = entities(i)
            val x1 = Math.floor((e.position.x - e.size.x * 0.5 - CollidingEntity.gapEpsilon) * cellFactor.x).toInt
            val x2 = Math.ceil((e.position.x + e.size.x * 0.5 + CollidingEntity.gapEpsilon) * cellFactor.x).toInt
            val y1 = Math.floor((e.position.y - e.size.y * 0.5 - CollidingEntity.gapEpsilon) * cellFactor.y).toInt
            val y2 = Math.ceil((e.position.y + e.size.y * 0.5 + CollidingEntity.gapEpsilon) * cellFactor.y).toInt
            var x = x1
            while(x <= x2) {
                var y = y1
                while(y <= y2) {
                    val k = x + "," + y
                    if(grid.contains(k)) {
                        grid(k).push(e)
                    } else {
                        grid(k) = js.Array(e)
                    }
                    y += 1
                }
                x += 1
            }
            i += 1
        }
    }
}
