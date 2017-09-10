package dk.mzw.scalasprites

import dk.mzw.scalasprites.SpriteGl.Shape
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture}
import scala.scalajs.js

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import SpriteCanvas._

object SpriteCanvas {

    class Loader(canvas: HTMLCanvasElement) {
        val gl = new SpriteGl(canvas)
        gl.initSpriteProgram()
        private var images = Set[String]()

        def apply(imageIrl: String): Image = {
            images += imageIrl
            Image(imageIrl, 0, 0, None, None)
        }

        def complete: Future[Display] = {
            PackImages(images.toList.distinct).map { case (image, mapping) =>
                val texture = gl.bindTexture(image)
                val textureMapping = mapping.map { case (url, p) =>
                    url -> StampTexture(p.x, p.y, p.rectangle.width, p.rectangle.height, image.width, image.height, texture)
                }
                new Display(gl, textureMapping)
            }
        }
    }

    case class Image(
        url: String,
        top: Int,
        left: Int,
        width: Option[Int],
        height: Option[Int]
    ) {
        def chop(top: Int = 0, left: Int = 0, width: Int = 0, height: Int = 0): Image = {
            copy(
                top = this.top + top,
                left = this.left + left,
                width = if (width <= 0) this.width.map(_ - left) else Some(width),
                height = if (height <= 0) this.height.map(_ - top) else Some(height)
            )
        }

        def splitArray(width: Int, frames: Int): Array[Image] = {
            (for (i <- 0 to frames) yield chop(left = i * width, width = width)).toArray
        }

        def split(width: Int, frames: Int): Double => Image = {
            val images: Array[Image] = splitArray(width, frames)

            { f: Double => images(((f % 1) * frames).toInt) }
        }
    }

    class Display(gl: SpriteGl, textures: Map[String, StampTexture]) {
        private val sprites = js.Array[Sprite]()
        var i = 0

        def add(image: Image, x: Double, y: Double, size: Double, angle: Double) {
            if (i < sprites.length) {
                val sprite = sprites(i)
                sprite.image = image
                sprite.x = x
                sprite.y = y
                sprite.size = size
                sprite.angle = angle
            }
            else {
                sprites.push(Sprite(image, x, y, size, angle))
            }
        }

        def draw(height: Double) {
            gl.clear()
            sprites/*.view(0, i + 1)*/.groupBy(s => textures(s.image.url).texture).foreach { case (texture, sprites) => // TODO sort in place
                gl.activateTexture(texture)
                val array = sprites.map { sprite =>
                    val stamp = textures(sprite.image.url)
                    val image = sprite.image
                    val textureLeft = (image.left + stamp.stampLeft).toDouble / stamp.atlasWidth
                    val textureTop = (image.top + stamp.stampTop).toDouble / stamp.atlasHeight
                    val textureWidth = image.width.map(_.toDouble / stamp.stampWidth).getOrElse(1d) * (stamp.stampWidth.toDouble / stamp.atlasWidth)
                    val textureHeight = image.height.map(_.toDouble / stamp.stampHeight).getOrElse(1d) * (stamp.stampHeight.toDouble / stamp.atlasHeight)

                    // TODO avoid allocating. Write direct to Float32Array
                    Shape(sprite.x, sprite.y, sprite.size, sprite.size, sprite.angle, textureLeft, textureTop, textureWidth, textureHeight)
                }.toArray
                gl.drawSprites(height, array)
            }
            i = 0
        }

        private case class Sprite(var image: Image, var x: Double, var y: Double, var size: Double, var angle: Double)

    }

    private case class StampTexture(
        stampLeft: Int,
        stampTop: Int,
        stampWidth: Int,
        stampHeight: Int,
        atlasWidth: Int,
        atlasHeight: Int,
        texture: WebGLTexture
    )

}
