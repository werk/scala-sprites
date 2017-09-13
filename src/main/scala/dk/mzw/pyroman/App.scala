package dk.mzw.pyroman

import dk.mzw.scalasprites.ScalaSprites.SpriteCanvas
import dk.mzw.scalasprites.{PackImages, ScalaSprites}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSApp

object App extends JSApp {

    def main() : Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val keys = new Keys()

        ScalaSprites.loadView(PyroMan.view, canvas, { spriteCanvas : SpriteCanvas[PyroMan.GameState] =>
            ScalaSprites.gameLoop(spriteCanvas, PyroMan.initialState, PyroMan.nextState(keys))
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
