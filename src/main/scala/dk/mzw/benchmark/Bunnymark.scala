package dk.mzw.benchmark

import dk.mzw.scalasprites.SpriteCanvas.{Display, Image, Loader}
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLCanvasElement, HTMLElement}

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.Implicits.global

object Bunnymark extends JSApp {
    def main(): Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)
        val game = new Bunnymark(loader)

        loader.complete.foreach{display =>
            println("Loader complete")

            def loop(): Unit = {
                game.update()
                game.draw(display)
                dom.window.requestAnimationFrame{_ => loop()}
            }
            loop()
        }
    }
}

private class Bunnymark(loader : Loader) {
    val fpsElement = dom.document.getElementById("fps").asInstanceOf[HTMLElement]
    val bunnyCountElement = dom.document.getElementById("bunnyCount").asInstanceOf[HTMLElement]

    val images = List(
        loader("bunnymark/rabbitv3.png"),
        loader("bunnymark/rabbitv3_ash.png"),
        loader("bunnymark/rabbitv3_batman.png"),
        loader("bunnymark/rabbitv3_bb8.png"),
        loader("bunnymark/rabbitv3_frankenstein.png"),
        loader("bunnymark/rabbitv3_neo.png"),
        loader("bunnymark/rabbitv3_sonic.png"),
        loader("bunnymark/rabbitv3_spidey.png"),
        loader("bunnymark/rabbitv3_stormtrooper.png"),
        loader("bunnymark/rabbitv3_superman.png"),
        loader("bunnymark/rabbitv3_tron.png"),
        loader("bunnymark/rabbitv3_wolverine.png")
    )

    var bunnies = List[Bunny]()
    val bounds = Bounds(
        left = -400,
        right = 400,
        bottom = -300,
        top = 300
    )

    var frames = 0
    var fpsTimerStart = System.currentTimeMillis()
    def update(): Unit = {
        if(frames % 30 == 0) {
            //addBunnies(100)
            val seconds = (System.currentTimeMillis() - fpsTimerStart).toDouble * 0.001
            val fps = frames / seconds
            fpsElement.innerHTML = fps.round.toString
        }
        bunnies.foreach(_.update())
    }

    val clearColor = (1d, 1d, 1d, 1d)
    def draw(display : Display): Unit = {
        bunnies.foreach{bunny =>
            display.add(bunny.image, bunny.x, bunny.y, 25, 0)
        }
        display.draw(clearColor, 600)
        frames += 1
    }

    def addBunnies(count : Int) : Unit = {
        val image = images((Math.random() * images.length).toInt)
        for(_ <- 0 until count) {
            bunnies ::= new Bunny(
                image = image,
                x = (bunnies.length * 2) % 800 - 400,
                y = 200,
                bounds = bounds
            )
        }
        bunnyCountElement.innerHTML = bunnies.size.toString
    }
    for(_ <- 0 until 100) {
        addBunnies(100)
    }

}

private class Bunny(val image : Image, var x : Double, var y : Double, bounds : Bounds) {
    var gravity = -0.75
    var speedX = Math.random() * 10
    var speedY = (Math.random() * 10) - 5

    def update(): Unit = {
        x += speedX
        y += speedY
        speedY += gravity

        if (x > bounds.right) {
            this.speedX *= -1
            x = bounds.right
        }
        else if (x < bounds.left) {
            this.speedX *= -1
            x = bounds.left
        }

        if (y < bounds.bottom) {
            this.speedY *= -0.85
            y = bounds.bottom
            if (Math.random > 0.5) this.speedY += Math.random * 6
        }
        else if (y > bounds.top) {
            this.speedY = 0
            y = bounds.top
        }
    }
}

private case class Bounds(left : Double, right : Double, bottom : Double, top : Double)
