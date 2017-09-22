package dk.mzw.scalasprites

import org.scalajs.dom
import org.scalajs.dom.raw.{WebGLRenderingContext => GL}
import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture}

import scala.scalajs.js
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SpriteCanvas {

    case class Blending(
        equation : Int,
        sourceFactor : Int,
        destinationFactor : Int
    ) {
        def show : String = if(this == Blending.top) "TOP" else if (this == Blending.additive) "ADD" else toString
    }

    object Blending {
        val top = Blending(GL.FUNC_ADD, GL.SRC_ALPHA, GL.ONE_MINUS_SRC_ALPHA)
        val additive = Blending(GL.FUNC_ADD, GL.ONE, GL.ONE)
        val substractive = Blending(GL.FUNC_SUBTRACT, GL.ONE, GL.ONE)
    }

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
                dom.document.body.appendChild(atlas) // TODO remove this
                completed = true
                val texture = gl.bindTexture(atlas)

                images.groupBy(_.url).foreach{case (url, group) =>
                    val p = mapping(url)
                    group.foreach{image =>
                        val stampLeft = p.x + image.left
                        val stampTop = p.y + image.top
                        val stampWidth = image.width.getOrElse(p.rectangle.width)
                        val stampHeight = image.height.getOrElse(p.rectangle.height)
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
                            textureLeft = stampLeft.toDouble / atlasWidth,
                            textureTop = stampTop.toDouble / atlasHeight,
                            textureWidth = stampWidth.toDouble / atlasWidth,
                            textureHeight = stampHeight.toDouble / atlasHeight
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

    case class Sprite(
        var image: Image,
        var x: Double,
        var y: Double,
        var height: Double,
        var angle: Double,
        var depth : Double,
        var blending : Blending,
        var index : Int
    )

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
        private val spriteBuffer = js.Array[Sprite]()
        private var addedSprites = 0

        def add(image: Image, x: Double, y: Double, height: Double, angle: Double, depth : Double = 0, blending : Blending = Blending.top) {
            if (addedSprites < spriteBuffer.length) {
                val sprite = spriteBuffer(addedSprites)
                sprite.image = image
                sprite.x = x
                sprite.y = y
                sprite.height = height
                sprite.angle = angle
                sprite.depth = depth
                sprite.blending = blending
                sprite.index = addedSprites
            }
            else {
                spriteBuffer.push(Sprite(image, x, y, height, angle, depth, blending, addedSprites))
            }
            addedSprites += 1
        }

        private def compare(a : Sprite, b : Sprite) : Int = {
            // Preserve element positions after the draw window
            if(a.index >= addedSprites && b.index >= addedSprites) return a.index - b.index
            if(a.index >= addedSprites) return 1
            if(b.index >= addedSprites) return -1

            val depth = a.depth - b.depth
            if(depth != 0) return Math.signum(depth).toInt

            val equation = a.blending.equation - b.blending.equation
            if(equation != 0) return equation

            val sourceFactor = a.blending.sourceFactor- b.blending.sourceFactor
            if(sourceFactor != 0) return sourceFactor

            val destinationFactor = a.blending.destinationFactor - b.blending.destinationFactor
            if(destinationFactor != 0) return destinationFactor

            a.index - b.index // Make it stable
        }

        var firstDraw = false
        def draw(clearColor : (Double, Double, Double, Double), height: Double, centerX : Double = 0, centerY : Double = 0) {
            gl.clear(clearColor)
            gl.resize(height, centerX, centerY)
            //spriteBuffer.sort(compare)

            if(spriteBuffer.length == 0) return // TODO

            var segmentStart = 0
            var segmentLength = 0
            var segmentSprite = spriteBuffer(segmentStart)

            var i = 0
            while(i < addedSprites) {
                val sprite = spriteBuffer(i)
                if(segmentSprite.blending != sprite.blending) {
                    gl.drawSprites(spriteBuffer, segmentStart, segmentLength)

                    segmentStart = i
                    segmentLength = 0
                    segmentSprite = sprite
                }

                segmentLength += 1
                i += 1
            }
            gl.drawSprites(spriteBuffer, segmentStart, segmentLength)

            addedSprites = 0
            firstDraw = false
        }
    }
}
