package dk.mzw.scalasprites

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture}

object ScalaSprites {

    case class Image(url : String)

    case class Scene(
        sprites : List[Sprite],
        backgroundColor : (Double, Double, Double, Double)  = (0, 0, 0, 1)
    )

    case class Sprite(
        x : Double,
        y : Double,
        image : Image
    )

    case class SpriteCanvas[S](
        canvas : HTMLCanvasElement,
        draw : S => Unit
    )

    type View[State] = (String => Image) => (State => Scene)

    def loadView[State](
        view : View[State],
        canvas : HTMLCanvasElement,
        onLoad : SpriteCanvas[State] => Unit
    ) : Unit = {
        val gl = new WebGl(canvas)
        gl.initSpriteProgram()
        var images = Set[String]()

        def load(url : String) : Image = {
            println(s"load $url")
            images += url
            Image(url)
        }

        val makeViewState : State => Scene = view(load)

        def draw(textures : Map[String, WebGLTexture])(state : State) : Unit = {
            val viewState = makeViewState(state)
            gl.clear()
            viewState.sprites.groupBy(_.image).foreach{case (image, sprites) =>
                val texture = textures(image.url)
                gl.activateTexture(texture)

                val array = sprites.map{s =>
                    (s.x, s.y, 1.0)
                }.toArray
                gl.drawPointSprites(array)
            }
        }

        gl.initTextures(images, {map =>
            val spriteCanvas = SpriteCanvas[State](canvas, draw(map))
            onLoad(spriteCanvas)
        })
    }

    def gameLoop[State](spriteCanvas : SpriteCanvas[State], initialState : State, step : (State, Double, Double) => State) : Unit = {
        var state = initialState
        var lastTime = System.currentTimeMillis() * 0.001
        def loop(): Unit = {
            val time = System.currentTimeMillis() * 0.001
            val dt = time - lastTime
            state = step(state, time, dt)
            spriteCanvas.draw(state)
            lastTime = time
            dom.window.requestAnimationFrame{_ => loop()}
        }
        loop()
    }
}
