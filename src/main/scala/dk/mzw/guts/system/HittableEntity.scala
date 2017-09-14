package dk.mzw.guts.system

import dk.mzw.guts.system.Entity._

/** An entity that can understand Hit messages */
trait HittableEntity extends PawnEntity

object HittableEntity {

    case class Hit(point : Vector2d, velocity : Vector2d, that : Self) extends Message

}
