package dk.mzw.scalasprites

import dk.mzw.scalasprites.SpriteGl.Shape
import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture}

import scala.collection.mutable
import scala.scalajs.js
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SpriteCanvas {

    class Loader(canvas: HTMLCanvasElement) {
        val gl = new SpriteGl(canvas)
        gl.initSpriteProgram()
        private val images = mutable.Map[String, Image]()

        def apply(imageIrl: String): Image = {
            images.getOrElseUpdate(imageIrl, Image(imageIrl, 0, 0, None, None))
        }

        def complete: Future[Display] = {
            PackImages(images.keys.toList).map { case (atlas, mapping) =>
                val texture = gl.bindTexture(atlas)
                mapping.foreach { case (url, p) =>
                    val image = images(url)

                    val stampLeft = p.x
                    val stampTop = p.y
                    val stampWidth = p.rectangle.width
                    val stampHeight = p.rectangle.height
                    val atlasWidth = atlas.width
                    val atlasHeight = atlas.height

                    val stamp = StampTexture(
                        stampLeft = stampLeft,
                        stampTop = stampTop,
                        stampWidth = stampWidth,
                        stampHeight = stampHeight,
                        atlasWidth = atlasWidth,
                        atlasHeight = atlasHeight,
                        texture = texture,
                        textureLeft = (image.left + stampLeft).toDouble / atlasWidth,
                        textureTop = (image.top + stampTop).toDouble / atlasHeight,
                        textureWidth = image.width.map(_.toDouble / stampWidth).getOrElse(1d) * (stampWidth.toDouble / atlasWidth),
                        textureHeight = image.height.map(_.toDouble / stampHeight).getOrElse(1d) * (stampHeight.toDouble / atlasHeight)
                    )

                    image.stamp = stamp
                }
                new Display(gl)
            }
        }
    }

    case class Sprite(var image: Image, var x: Double, var y: Double, var size: Double, var angle: Double)

    case class StampTexture(
        stampLeft: Int,
        stampTop: Int,
        stampWidth: Int,
        stampHeight: Int,
        atlasWidth: Int,
        atlasHeight: Int,
        texture: WebGLTexture,
        textureLeft : Double,
        textureTop : Double,
        textureWidth : Double,
        textureHeight : Double
    )

    case class Image(
        url: String,
        top: Int,
        left: Int,
        width: Option[Int],
        height: Option[Int],
        var stamp : StampTexture = null
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

    class Display(gl: SpriteGl) {
        private val sprites = js.Array[Sprite]()
        private var i = 0

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
            i += 1
        }

        def draw(height: Double) {
            gl.clear()
            gl.drawSprites(height, sprites)
            /*val array = sprites.map { sprite =>
                val stamp = sprite.image.stamp
                gl.activateTexture(stamp.texture)

                // TODO avoid allocating. Write direct to Float32Array
                Shape(sprite.x, sprite.y, sprite.size, sprite.size, sprite.angle, stamp.textureLeft, stamp.textureTop, stamp.textureWidth, stamp.textureHeight)
            }.toArray
            gl.drawSprites(height, array)*/
            i = 0
        }
    }
}
