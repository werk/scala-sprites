package dk.mzw.scalasprites

import dk.mzw.scalasprites.SpriteGl.Shape
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture}

import scala.concurrent.ExecutionContext.Implicits.global

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
        val gl = new SpriteGl(canvas)
        gl.initSpriteProgram()
        var images = Set[String]()

        val loader = new ImageLoader() {
            override def apply(url: String) = {
                images += url
                Image(url, 0, 0, None, None)
            }
        }

        val makeViewState : State => Scene = view(loader)

        def draw(textures : Map[String, StampTexture])(state : State) : Unit = {
            val viewState = makeViewState(state)
            gl.clear()
            viewState.sprites.groupBy(s => textures(s.image.url).texture).foreach{ case (texture, sprites) =>
                gl.activateTexture(texture)
                val array = sprites.map{sprite =>
                    val stamp = textures(sprite.image.url)
                    val image = sprite.image
                    val textureLeft = (image.left + stamp.stampLeft).toDouble / stamp.atlasWidth
                    val textureTop = (image.top + stamp.stampTop).toDouble / stamp.atlasHeight
                    val textureWidth = image.width.map(_.toDouble / stamp.stampWidth).getOrElse(1d) * (stamp.stampWidth.toDouble / stamp.atlasWidth)
                    val textureHeight = image.height.map(_.toDouble / stamp.stampHeight).getOrElse(1d) * (stamp.stampHeight.toDouble / stamp.atlasHeight)

                    Shape(sprite.x, sprite.y, sprite.size, sprite.size, sprite.angle, textureLeft, textureTop, textureWidth, textureHeight)
                }.toArray
                gl.drawSpritesSlow(viewState.height, array)
            }
        }

        PackImages(images.toList.distinct).foreach{case (image, mapping) =>
            val texture = gl.bindTexture(image)
            val textureMapping = mapping.map{case (url, p) =>
                url -> StampTexture(p.x, p.y, p.rectangle.width, p.rectangle.height, image.width, image.height, texture)
            }
            val spriteCanvas = SpriteCanvas[State](canvas, draw(textureMapping))
            onLoad(spriteCanvas)
        }

        /*gl.initTextures(images, {map =>
            val spriteCanvas = SpriteCanvas[State](canvas, draw(map))
            onLoad(spriteCanvas)
        })*/
    }

    case class StampTexture(
        stampLeft : Int,
        stampTop : Int,
        stampWidth : Int,
        stampHeight : Int,
        atlasWidth : Int,
        atlasHeight : Int,
        texture : WebGLTexture
    )

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
