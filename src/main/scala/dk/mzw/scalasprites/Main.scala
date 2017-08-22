package dk.mzw.scalasprites

import dk.mzw.scalasprites.ScalaSprites.SpriteCanvas
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.JSApp

object Main extends JSApp {

    def main() : Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]

        ScalaSprites.loadView(PyroMan.view, canvas, { spriteCanvas : SpriteCanvas[PyroMan.GameState] =>
            ScalaSprites.gameLoop(spriteCanvas, PyroMan.initialState, PyroMan.nextState)
        })

    }

}
