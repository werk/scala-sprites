package dk.mzw.scalasprites

import dk.mzw.scalasprites.ScalaSprites._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

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
        println("loadView")
        val gl = new WebGl(canvas)
        gl.initSpriteProgram()
        //var images = Map[String, Int]()
        var image : String = null

        def load(url : String) : Image = {
            //images += url -> 0 // TODO
            println(s"load $url")
            image = url
            Image(url)
        }

        val makeViewState : State => Scene = view(load)

        def draw(state : State) : Unit = {
            val viewState = makeViewState(state)
            val array = viewState.sprites.map{s =>
                (s.x, s.y, 1.0)
            }.toArray
            gl.drawPointSprites(array)
        }

        gl.loadTexture(image, {url =>
            println(s"loadTexture $url")
            val spriteCanvas = SpriteCanvas[State](canvas, draw)
            onLoad(spriteCanvas)
        })
    }
}
