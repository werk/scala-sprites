package dk.mzw.puzzle

import dk.mzw.guts.Guts.secondsElapsed
import dk.mzw.scalasprites.SpriteCanvas.{Display, Loader}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSApp

abstract class GameLoop(canvasId : String) extends JSApp {

    def load(loader : Loader)
    def onLoad(display : Display)
    def update(display : Display, t : Double, dt : Double)

    var canvas : HTMLCanvasElement = _

    override def main(): Unit = {
        canvas = dom.document.getElementById(canvasId).asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)

        load(loader)

        loader.complete.foreach { display =>
            onLoad(display)

            // This crazy stuff is done to avoid creating and allocating a new anonymous function for each call to requestAnimationFrame
            var loopF : Double => Unit = null
            val start : Double = secondsElapsed()
            var t0 = start
            def loop(_t : Double) : Unit = {
                val t = secondsElapsed() - start
                val dt = t - t0
                t0 = t
                update(display, t, dt)
                dom.window.requestAnimationFrame(loopF)
            }
            loopF = loop
            loop(0)
        }
    }
}
