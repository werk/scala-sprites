package dk.mzw.guts.entities

import dk.mzw.guts.entities.MortalEntity.Damage
import dk.mzw.guts.system.Entity.Message
import dk.mzw.guts.system.ReceivingEntity

trait MortalEntity extends ReceivingEntity {

    var health : Double

    def onMessage(message : Message) : Unit = message match {
        case m : Damage =>
            health -= m.amount
            if(health < 0) onDie()
    }

    def onDie() : Unit

}

object MortalEntity {
    case class Damage(amount : Double) extends Message
}