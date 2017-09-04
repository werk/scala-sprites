package dk.mzw.scalasprites

import dk.mzw.scalasprites.ScalaSprites.SpriteCanvas
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends JSApp {

    def main() : Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]

        ScalaSprites.loadView(PyroMan.view, canvas, { spriteCanvas : SpriteCanvas[PyroMan.GameState] =>
            ScalaSprites.gameLoop(spriteCanvas, PyroMan.initialState, PyroMan.nextState)
        })

        PackImages(List(
            "assets/topman.png",
            "assets/topman-shooting.png",
            "assets/flame-bright.png",
            "assets/flame-red.png")
        ).foreach{case (image, mapping) =>
            mapping.foreach {case (url, r) => println(s"$url: $r")}
            dom.document.body.appendChild(image)
        }

    }

}
