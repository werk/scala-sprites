package dk.mzw.guts.system

trait UpdateableEntity extends Entity {

    def onUpdate(world : WorldEntity, delta : Double) : Unit

}
