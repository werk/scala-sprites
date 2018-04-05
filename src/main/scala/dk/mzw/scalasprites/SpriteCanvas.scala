package dk.mzw.scalasprites

import dk.mzw.scalashading.Language
import dk.mzw.scalashading.Language.R
import dk.mzw.scalashading.internal.Internal.Uniform
import dk.mzw.scalasprites.SpriteGl.Shader
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture, WebGLRenderingContext => GL}

import scala.scalajs.js
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SpriteCanvas {

    trait BoundingBox {
        def x1 : Double
        def y1 : Double
        def x2 : Double
        def y2 : Double
        def height = y2 - y1
        def width = x2 - x1
        def centerX = x1 + 0.5 * width
        def centerY = y1 + 0.5 * height
    }

    case class Blending(
        equation : Int,
        sourceFactor : Int,
        destinationFactor : Int,
        constantColor : Option[(Double, Double, Double, Double)] = None
    ) {
        def show : String = if(this == Blending.top) "TOP" else if (this == Blending.additive) "ADD" else toString
    }

    object Blending {
        val top = Blending(GL.FUNC_ADD, GL.SRC_ALPHA, GL.ONE_MINUS_SRC_ALPHA)
        val additive = Blending(GL.FUNC_ADD, GL.ONE, GL.ONE)
        val substractive = Blending(GL.FUNC_SUBTRACT, GL.ONE, GL.ONE)
    }

    trait CustomShader {
        def shader : Shader
    }

    trait Image extends CustomShader {
        val url: String
        val top: Int
        val left: Int
        val width: Option[Int]
        val height: Option[Int]
        val repeat : Boolean
        val shader : Shader
        def stamp : StampTexture

        def chop(top: Int = 0, left: Int = 0, width: Int = 0, height: Int = 0): Image
        def splitArray(width: Int, frames: Int): Array[Image]
        def split(width: Int, frames: Int): Double => Image
    }


    case class MutableCustomShader(
        image : Language.Image,
        uniforms : List[Uniform[Double]],
        uniformValues : List[Double] = List(),
        var shader : Shader = null
    ) extends CustomShader

    class Loader(canvas: HTMLCanvasElement) {
        val gl = new SpriteGl(canvas)
        private var images = List[MutableImage]()
        private var animations = List[MutableCustomShader]()
        private var completed = false

        def apply(imageUrl: String, repeat : Boolean = false) : Image = {
            val image = MutableImage(imageUrl, 0, 0, None, None, repeat, gl.spriteShader)
            images ::= image
            image
        }

        def apply(animation : Language.Image) : CustomShader = {
            val a = MutableCustomShader(animation, List())
            animations ::= a
            a
        }

        def apply(f : R => Language.Image) : Double => CustomShader = {
            val u = new Uniform[Double]("u", 0)
            val r = Language.liftUniformR(u)
            val a = MutableCustomShader(f(r), List(u))
            animations ::= a

            {v => a.copy(uniformValues = List(v))}
        }

        // TODO generalize this
        def f2(f : R => R => Language.Image) : Double => Double => CustomShader = {
            val u1 = new Uniform[Double]("u1", 0)
            val u2 = new Uniform[Double]("u2", 0)
            val a = MutableCustomShader(f(Language.liftUniformR(u1))(Language.liftUniformR(u2)), List(u1, u2))
            animations ::= a

            {v1 => v2 => a.copy(uniformValues = List(v1, v2))}
        }

        def f3(f : R => R => R => Language.Image) : Double => Double => Double => CustomShader = {
            val u1 = new Uniform[Double]("u1", 0)
            val u2 = new Uniform[Double]("u2", 0)
            val u3 = new Uniform[Double]("u3", 0)
            val a = MutableCustomShader(f(Language.liftUniformR(u1))(Language.liftUniformR(u2))(Language.liftUniformR(u3)), List(u1, u2, u3))
            animations ::= a

            {v1 => v2 => v3 => a.copy(uniformValues = List(v1, v2, v3))}
        }

        def f4(f : R => R => R => R => Language.Image) : Double => Double => Double => Double => CustomShader = {
            val u1 = new Uniform[Double]("u1", 0)
            val u2 = new Uniform[Double]("u2", 0)
            val u3 = new Uniform[Double]("u3", 0)
            val u4 = new Uniform[Double]("u4", 0)
            val a = MutableCustomShader(f(Language.liftUniformR(u1))(Language.liftUniformR(u2))(Language.liftUniformR(u3))(Language.liftUniformR(u4)), List(u1, u2, u3, u4))
            animations ::= a

            {v1 => v2 => v3 => v4 => a.copy(uniformValues = List(v1, v2, v3, v4))}
        }

        def f5(f : R => R => R => R => R => Language.Image) : Double => Double => Double => Double => Double => CustomShader = {
            val u1 = new Uniform[Double]("u1", 0)
            val u2 = new Uniform[Double]("u2", 0)
            val u3 = new Uniform[Double]("u3", 0)
            val u4 = new Uniform[Double]("u4", 0)
            val u5 = new Uniform[Double]("u5", 0)
            val a = MutableCustomShader(f(Language.liftUniformR(u1))(Language.liftUniformR(u2))(Language.liftUniformR(u3))(Language.liftUniformR(u4))(Language.liftUniformR(u5)), List(u1, u2, u3, u4, u5))
            animations ::= a

            {v1 => v2 => v3 => v4 => v5 => a.copy(uniformValues = List(v1, v2, v3, v4, v5))}
        }

        def complete: Future[Display] = {
            // TODO remove duplicates
            PackImages(images).map { case (atlas, mapping) =>
                //dom.document.body.appendChild(atlas) // TODO remove this
                completed = true
                val texture = gl.bindTexture(atlas)

                mapping.foreach{case (i, p) =>
                    val image = i.asInstanceOf[MutableImage]
                    val stampLeft = p.x + image.left
                    val stampTop = p.y + image.top
                    val stampWidth = image.width.getOrElse(p.rectangle.width)
                    val stampHeight = image.height.getOrElse(p.rectangle.height)
                    val atlasWidth = atlas.width
                    val atlasHeight = atlas.height

                    val stamp = StampTexture(
                        stampAspectRatio = stampWidth.toDouble / stampHeight,
                        texture = texture,
                        textureLeft = stampLeft.toDouble / atlasWidth,
                        textureTop = stampTop.toDouble / atlasHeight,
                        textureWidth = stampWidth.toDouble / atlasWidth,
                        textureHeight = stampHeight.toDouble / atlasHeight
                    )

                    image.stamp = stamp
                }

                animations.foreach{a =>
                    val shader = gl.initPixelProgram(a.image, a.uniforms)
                    a.shader = shader
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
            repeat : Boolean,
            shader : Shader,
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
        var image : CustomShader,
        var imageX : Float,
        var imageY : Float,
        var imageWidth : Float,
        var imageHeight : Float,
        var x: Double,
        var y: Double,
        var width: Double,
        var height: Double,
        var angle: Double,
        var depth : Double,
        var blending : Blending,
        var index : Int
    )

    case class StampTexture(
        stampAspectRatio : Double,
        texture: WebGLTexture,
        textureLeft : Double,
        textureTop : Double,
        textureWidth : Double,
        textureHeight : Double
    )

    class Display(gl: SpriteGl) {
        private val spriteBuffer = js.Array[Sprite]()
        private var addedSprites = 0

        val boundingBox = gl.boundingBox

        def gameCoordinatesX(pixelX : Double) : Double = {
            val rx = pixelX / gl.canvas.clientWidth
            //boundingBox.width * (rx - 0.5)
            rx * boundingBox.width + boundingBox.x1
        }

        def gameCoordinatesY(pixelY : Double) : Double = {
            val ry = (gl.canvas.clientHeight - pixelY) / gl.canvas.clientHeight
            //boundingBox.height * (ry - 0.5)
            ry * boundingBox.height + boundingBox.y1
        }

        def add(
            image : CustomShader,
            x: Double,
            y: Double,
            height: Double = 1,
            angle: Double = 0,
            depth : Double = 0,
            blending : Blending = Blending.top,
            width : Double = 0,
            imageX : Double = -1,
            imageY : Double = -1,
            imageWidth : Double = 2,
            imageHeight : Double = 2
        ) {
            if (addedSprites < spriteBuffer.length) {
                val sprite = spriteBuffer(addedSprites)
                sprite.image = image
                sprite.imageX = imageX.toFloat
                sprite.imageY = imageY.toFloat
                sprite.imageWidth = imageWidth.toFloat
                sprite.imageHeight = imageHeight.toFloat
                sprite.x = x
                sprite.y = y
                sprite.width = width
                sprite.height = height
                sprite.angle = angle
                sprite.depth = depth
                sprite.blending = blending
                sprite.index = addedSprites
            }
            else {
                spriteBuffer.push(Sprite(image, imageX.toFloat, imageY.toFloat, imageWidth.toFloat, imageHeight.toFloat, x, y, width, height, angle, depth, blending, addedSprites))
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
                if(true/*segmentSprite.blending != sprite.blending || segmentSprite.image.shader != sprite.image.shader*/) {
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
