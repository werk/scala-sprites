package dk.mzw.guts.system

import dk.mzw.scalasprites.Measure

import scala.scalajs.js

/** An entity that can hit hittable entities */
trait HittingEntity extends PawnEntity {

    val zero = Vector2d(0, 0)

    def onHit(world : WorldEntity, that : HittableEntity)

    def internalEmitHits(world : WorldEntity) = {
        val x1 = position.x - size.x * 0.5
        val x2 = position.x + size.x * 0.5
        val y1 = position.y - size.y * 0.5
        val y2 = position.y + size.y * 0.5
        world.hittableGrid.find(position, zero, 0, size)
        var i = 0
        while(i < world.hittableGrid.foundCount) {
            val entity = world.hittableGrid.found(i)
            if(entity != this) {
                Measure.count("internalEmitHits loop")
                val x3 = entity.position.x - entity.size.x * 0.5
                val x4 = entity.position.x + entity.size.x * 0.5
                val y3 = entity.position.y - entity.size.y * 0.5
                val y4 = entity.position.y + entity.size.y * 0.5
                if (x1 <= x4 && x3 <= x2 && y1 <= y4 && y3 <= y2) {
                    onHit(world, entity)
                }
            }
            i += 1
        }
    }

}
