package dk.mzw.scalasprites

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.JSApp

object Main extends JSApp {

    def main() : Unit = {
        println("Guts")
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        Guts.run(canvas)
    }

}
