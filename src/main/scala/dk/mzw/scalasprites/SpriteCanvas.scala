package dk.mzw.scalasprites

import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture}

import scala.collection.mutable
import scala.scalajs.js
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SpriteCanvas {

    trait Image{
        val url: String
        val top: Int
        val left: Int
        val width: Option[Int]
        val height: Option[Int]
        def stamp : StampTexture

        def chop(top: Int = 0, left: Int = 0, width: Int = 0, height: Int = 0): Image
        def splitArray(width: Int, frames: Int): Array[Image]
        def split(width: Int, frames: Int): Double => Image
    }

    class Loader(canvas: HTMLCanvasElement) {
        val gl = new SpriteGl(canvas)
        gl.initSpriteProgram()
        private var images = List[MutableImage]()
        private var completed = false

        def apply(imageIrl: String): Image = {
            val image = MutableImage(imageIrl, 0, 0, None, None)
            images ::= image
            image
        }

        def complete: Future[Display] = {
            PackImages(images.map(_.url).distinct).map { case (atlas, mapping) =>
                completed = true
                val texture = gl.bindTexture(atlas)

                images.groupBy(_.url).foreach{case (url, group) =>
                    val p = mapping(url)
                    group.foreach{image =>
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
                }

                new Display(gl)
            }
        }

        private case class MutableImage(
            url: String,
            top: Int,
            left: Int,
            width: Option[Int],
            height: Option[Int],
            var stamp : StampTexture = null
        ) extends Image {

            def chop(top: Int = 0, left: Int = 0, width: Int = 0, height: Int = 0): Image = {
                val image = copy(
                    top = this.top + top,
                    left = this.left + left,
                    width = if (width <= 0) this.width.map(_ - left) else Some(width),
                    height = if (height <= 0) this.height.map(_ - top) else Some(height)
                )
                images ::= image // TODO: This is the only reason why this class in nested in the loader. Consider removing this method from the class.
                image
            }

            def splitArray(width: Int, frames: Int): Array[Image] = {
                (for (i <- 0 to frames) yield chop(left = i * width, width = width)).toArray
            }

            def split(width: Int, frames: Int): Double => Image = {
                val images: Array[Image] = splitArray(width, frames)

                { f: Double => images(((f % 1) * frames).toInt) }
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
            gl.drawSprites(height, sprites, i)
            i = 0
        }
    }
}
