package dk.mzw.guts.system

import dk.mzw.pyroman.Keys

trait ControlledEntity extends Entity {

    def onInput(world : WorldEntity, keys: Keys) : Unit

}
