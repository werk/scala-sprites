package dk.mzw.benchmark

import dk.mzw.scalasprites.SpriteCanvas.{Blending, Display, Image, Loader}
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLCanvasElement, HTMLElement}

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object BlendingTest extends JSApp {
    def main(): Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)
        val game = new BlendingTest(loader)

        loader.complete.foreach{display =>
            def loop(): Unit = {
                game.draw(display)
                dom.window.requestAnimationFrame{_ => loop()}
            }
            loop()
        }
    }
}

private class BlendingTest(loader : Loader) {

    trait Draw { def draw(t : Double, d : Display) : Unit }

    val clearColor = (0.3, 0.3, 0.3, 1.0)


    val wallImage = loader("bunnymark/rabbitv3.png")
    val manImage = loader("bunnymark/rabbitv3_batman.png")
    val cloudImage = loader("bunnymark/rabbitv3_stormtrooper.png")
    val flameBrightImage = loader("assets/flame-bright.png")
    val flameRedImage = loader("assets/flame-red.png")

    val walls : List[Draw] = for(x <- (-10 to 10).toList; y <- -10 to 10 if Math.round(Math.sqrt(x*x + y*y)) % 3 == 0) yield new Draw {
        override def draw(t: Double, d: Display): Unit = {
            d.add(wallImage, x, y, 1, 0, depth = 0, Blending.top)
        }
    }

    val man = new Draw {
        override def draw(t: Double, d: Display): Unit = {
            d.add(manImage, Math.cos(t * 0.3) * 8, Math.sin(t * 0.4) * 3, 1, 0, depth = 1, Blending.top)
        }
    }

    val fire = for(i <- (0 until 100).toList) yield {
        new Draw {
            override def draw(t: Double, d: Display): Unit = {
                val v = Math.PI * 2 * 100 / i
                val r = (Math.cos(t) + 1) * 6
                d.add(flameRedImage, Math.cos(v) * r,  Math.sin(v) * r, 1, r, depth = 2, Blending.additive)
            }
        }
    }

    val clouds = for{i <- (0 to 50).toList} yield new Draw {
        val x = (Math.random() - 0.5) * 20
        val y = (Math.random() - 0.5) * 20
        override def draw(t: Double, d: Display): Unit = {
            d.add(cloudImage, x, y, 1, 0, depth = 3, Blending.top)

        }
    }

    def draw(display : Display): Unit = {
        val t = System.currentTimeMillis() * 0.001
        /*Random.shuffle*/(man :: walls ++ fire ++ clouds).foreach{_.draw(t, display)}
        display.draw(clearColor, 20)
    }

}
