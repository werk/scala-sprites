package dk.mzw.benchmark

import dk.mzw.scalasprites.SpriteCanvas.{Display, Loader}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.Implicits.global

class Benchmark extends JSApp {
    def main(): Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)
        val game = new MyGame(loader)

        loader.complete.foreach{display =>

            var lastTime = System.currentTimeMillis() * 0.001
            def loop(): Unit = {
                val time = System.currentTimeMillis() * 0.001
                val dt = time - lastTime
                game.draw(display, time)
                lastTime = time
                dom.window.requestAnimationFrame{_ => loop()}
            }
            loop()

            game.draw(display, 0)
        }
    }
}

private class MyGame(loader : Loader) {
    val man = loader("assets/topman.png").chop(24, 24)
    val flame = loader("assets/flame-bright.png")

    def draw(display : Display, t : Double): Unit = {
        display.add(man, 0, 0, 1.2, 0)
        for(i <- 1 to 100) {
            display.add(man, Math.sin(t * i), Math.cos(t * i), 1.2, 0)
        }
        display.draw(40)
    }

}
