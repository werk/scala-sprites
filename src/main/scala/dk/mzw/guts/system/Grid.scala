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
        val x1 = ((from.x - size.x) * Grid.cellFactor.x).toInt
        val x2 = ((to.x + size.x) * Grid.cellFactor.x).toInt
        val y1 = ((from.y - size.y) * Grid.cellFactor.y).toInt
        val y2 = ((to.y + size.y) * Grid.cellFactor.y).toInt
        var x = x1
        var y = y1
        foundCount = 0
        while(x <= x2) {
            while(y <= y2) {
                val k = x + "," + y
                if(Grid.grid.contains(k)) {
                    var entities = Grid.grid(k)
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
            val x1 = ((e.position.x - e.size.x) * cellFactor.x).toInt
            val x2 = ((e.position.x + e.size.x) * cellFactor.x).toInt
            val y1 = ((e.position.y - e.size.y) * cellFactor.y).toInt
            val y2 = ((e.position.y + e.size.y) * cellFactor.y).toInt
            var x = x1
            var y = y1
            while(x <= x2) {
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
