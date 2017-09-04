package dk.mzw.scalasprites

import dk.mzw.scalasprites.gl.WebGl.{LoadedTexture, Shape}
import dk.mzw.scalasprites.gl.QuadGl
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

object ScalaSprites {

    case class Image(
        url : String,
        top : Int,
        left : Int,
        width : Option[Int],
        height : Option[Int]
    ) {
        def chop(top : Int = 0, left : Int = 0, width : Int = 0, height : Int = 0) : Image = {
            copy(
                top = this.top + top,
                left = this.left + left,
                width = if(width <= 0) this.width.map(_ - left) else Some(width),
                height = if(height <= 0) this.height.map(_ - top) else Some(height)
            )
        }

        def splitArray(width : Int, frames : Int) : Array[Image] = {
            (for(i <- 0 to frames) yield chop(left = i * width, width = width)).toArray
        }

        def split(width : Int, frames : Int) : Double => Image = {
            val images : Array[Image] = splitArray(width, frames)

            {f : Double => images(((f % 1) * frames).toInt)}
        }
    }

    case class Scene(
        sprites : List[Sprite],
        height : Double,
        backgroundColor : (Double, Double, Double, Double)  = (0, 0, 0, 1)
    )

    case class Sprite(
        x : Double,
        y : Double,
        image : Image,
        size : Double,
        angle : Double
    )

    case class SpriteCanvas[S](
        canvas : HTMLCanvasElement,
        draw : S => Unit
    )

    trait ImageLoader {
        def apply(url : String) : Image
    }

    type View[State] = ImageLoader => (State => Scene)

    def loadView[State](
        view : View[State],
        canvas : HTMLCanvasElement,
        onLoad : SpriteCanvas[State] => Unit
    ) : Unit = {
        val gl = new QuadGl(canvas)
        gl.initSpriteProgram()
        var images = Set[String]()

        val loader = new ImageLoader() {
            override def apply(url: String) = {
                images += url
                Image(url, 0, 0, None, None)
            }
        }

        val makeViewState : State => Scene = view(loader)

        def draw(textures : Map[String, LoadedTexture])(state : State) : Unit = {
            val viewState = makeViewState(state)
            gl.clear()
            viewState.sprites.groupBy(_.image.url).foreach{case (url, sprites) =>
                val texture = textures(url)
                gl.activateTexture(texture.texture)
                val array = sprites.map{sprite =>
                    val image = sprite.image
                    val textureLeft = image.left.toDouble / texture.width
                    val textureTop = image.top.toDouble / texture.height
                    val textureWidth = image.width.map(_.toDouble / texture.width).getOrElse(1d)
                    val textureHeight = image.height.map(_.toDouble / texture.height).getOrElse(1d)

                    Shape(sprite.x, sprite.y, sprite.size, sprite.size, sprite.angle, textureLeft, textureTop, textureWidth, textureHeight)
                }.toArray
                gl.drawSprites(viewState.height, array)
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
