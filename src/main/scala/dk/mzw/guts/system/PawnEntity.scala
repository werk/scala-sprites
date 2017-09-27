package dk.mzw.guts.system

import dk.mzw.guts.system.Entity._

/** An entity that has a location on the world map */
trait PawnEntity extends Entity {
    val position : Vector2d
    val size : Vector2d
    val temporary = Vector2d(0, 0)
}
