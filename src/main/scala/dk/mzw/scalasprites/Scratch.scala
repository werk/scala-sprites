package dk.mzw.scalasprites

import dk.mzw.scalasprites.FutureSprites._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.concurrent.Future
import scala.scalajs.js.JSApp

object Scratch extends JSApp {
    def main() = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new SpriteLoader(canvas)
        val ball : SpriteImage = loader.load("images/ball.png")
        val flame : SpriteImage  = loader.load("images/flame.png")
        val future : Future[SpriteCanvas] = loader.finish

        def draw(t : Double, c : SpriteCanvas) : Unit = {
            for(i <- 1 to 1000) {
                val image = if(i % 2 == 0) ball else flame
                val it = i + t
                val r = Math.sin(it * 0.013)
                c.draw(
                    image = image,
                    x = Math.cos(it) * r,
                    y = Math.sin(it) * r
                )
            }
        }

        println("Loading...")
        import scala.concurrent.ExecutionContext.Implicits.global
        future.foreach{c =>
            println("Loading done")
            def loop(): Unit = {
                draw(System.currentTimeMillis() * 0.001, c)
                dom.window.requestAnimationFrame{_ => loop()}
            }
            loop()
        }
    }
}

object FutureSprites {
    trait SpriteImage

    trait SpriteCanvas {
        def draw(
            image : SpriteImage,
            x : Double,
            y : Double
        )
    }

    class SpriteLoader(canvas : HTMLCanvasElement) {
        def load(imageUrl : String) : SpriteImage = ???
        def finish : Future[SpriteCanvas] = ???
    }

}