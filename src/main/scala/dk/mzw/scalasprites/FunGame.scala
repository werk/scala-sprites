package dk.mzw.scalasprites

import dk.mzw.scalasprites.ScalaSprites.{Image, ImageLoader}

trait Game {
    def loadTextures(load : ImageLoader)
    def draw(p : Painter)
    def update(t : Double, dt : Double) : Unit
}

trait FunGame[State] {
    def draw(p : Painter, state : State) : Unit
    def update(t : Double, dt : Double) : State
    val initialState : State
}



trait Painter {
    def drawSprite(
        x : Double,
        y : Double,
        image : Image,
        size : Double,
        angle : Double
    )
}