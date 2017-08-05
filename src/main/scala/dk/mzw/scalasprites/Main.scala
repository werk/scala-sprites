package dk.mzw.scalasprites

import dk.mzw.scalasprites.ScalaSprites.SpriteCanvas
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.JSApp

object Main extends JSApp {

    def main() : Unit = {
        println("Guts")
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]

        ScalaSprites.loadView(Guts.view, canvas, { spriteCanvas : SpriteCanvas[Guts.GameState] =>
            ScalaSprites.gameLoop(spriteCanvas, Guts.initialState, Guts.nextState)
        })

    }

}
