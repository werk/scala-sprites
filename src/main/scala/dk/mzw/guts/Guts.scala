package dk.mzw.guts

import dk.mzw.guts.entities.BunnyEntity
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system.{GameWorld, Vector2d}
import dk.mzw.scalasprites.SpriteCanvas.Loader
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.Implicits.global

object Guts extends JSApp {
    def main(): Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)

        val batmanSprite = loader("bunnymark/rabbitv3_batman.png")

        val game = new GameWorld(loader, Seq(new BunnyEntity(Self("nananana", "batman"), Vector2d(0, 0), batmanSprite)))

        loader.complete.foreach { display =>
            println("Loader complete")

            def loop(last : Double) : Unit = {
                val now = secondsElapsed()
                val delta = now - last
                if(delta < 1) {
                    game.update(delta)
                    game.draw(display)
                }
                dom.window.requestAnimationFrame{_ => loop(now)}
            }
            loop(secondsElapsed() - 0.01)
        }
    }

    private def secondsElapsed() : Double = {
        scalajs.js.Dynamic.global.performance.now().asInstanceOf[Double] * 0.001
    }
}
