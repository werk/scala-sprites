package dk.mzw.scalasprites

import dk.mzw.accelemation.{Compile, Language}
import dk.mzw.accelemation.internal.Internal
import dk.mzw.accelemation.internal.Internal.Uniform
import dk.mzw.scalasprites.SpriteCanvas.{BoundingBox, Image, Sprite}
import dk.mzw.scalasprites.SpriteGl.Shader
import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw._

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class SpriteGl(val canvas : HTMLCanvasElement) {

    val gl = SpriteGl.getContexts(canvas)
    private val mutableBoundingBox = MutableBoundingBox(-1, -1, 1, 1)
    val boundingBox : BoundingBox = mutableBoundingBox

    private case class MutableBoundingBox(
        var x1 : Double,
        var y1 : Double,
        var x2 : Double,
        var y2 : Double
    ) extends BoundingBox

    private var activeTexture : WebGLTexture = _

    private val vertexPerSprite = 6
    private var vertexBuffersSpriteSize = 10000

    private val coordinatesBuffer : WebGLBuffer = gl.createBuffer()
    private val coordinatesBufferItemSize = 4
    private var coordinatesBufferArray : Float32Array = _

    private val rotationBuffer : WebGLBuffer = gl.createBuffer()
    private val rotationsBufferItemSize = 3
    private var rotationsBufferArray : Float32Array = _

    val spriteShader = initSpriteProgram()

    resizeBuffers(vertexBuffersSpriteSize)

    def initSpriteProgram(fragmentCode : String, uniform : Option[Uniform[Double]]) : Shader = {
        val vertexCode = s"""
            attribute vec4 a_coordinates;
            attribute vec3 a_rotation;
            uniform vec2 u_scale;
            uniform vec2 u_offset;

            varying vec2 v_textureCoordinates;

            void main(void) {
                float angle = a_rotation.z;
                float s = sin(angle);
                float c = cos(angle);
                vec2 center = a_rotation.xy;
                mat2 rotate = mat2(c, s, -s, c);
                vec2 p = rotate * (a_coordinates.xy - center) + center;
                gl_Position = vec4(p * u_scale + u_offset, 1.0, 1.0);
                v_textureCoordinates = a_coordinates.zw;
            }
        """

        val program = SpriteGl.initProgram(gl, vertexCode, fragmentCode)
        gl.useProgram(program)

        Shader(
            program = program,
            samplerUniformLocation = gl.getUniformLocation(program, "u_sampler"),
            scaleUniformLocation = gl.getUniformLocation(program, "u_scale"),
            offsetUniformLocation = gl.getUniformLocation(program, "u_offset"),
            parameter1UniformLocation = uniform.map(u => gl.getUniformLocation(program, u.name) -> u),
            coordinatesAttributeLocation = gl.getAttribLocation(program, "a_coordinates"),
            rotationsAttributeLocation = gl.getAttribLocation(program, "a_rotation")
        )
    }

    def initSpriteProgram() : Shader = {
        val fragmentCode = """
            precision highp float;

            uniform sampler2D u_sampler;
            varying vec2 v_textureCoordinates;

            void main(void) {
              gl_FragColor = texture2D(u_sampler, v_textureCoordinates);
            }
        """
        initSpriteProgram(fragmentCode, None)
    }

    def initPixelProgram(f : Language.Image, uniform : Option[Uniform[Double]]) : Shader = {
        val fragmentSource = Compile.image(f, Language.Vec2(Internal.BuiltIn("v_textureCoordinates")))
        initSpriteProgram(fragmentSource, uniform)
    }

    def clear(clearColor : (Double, Double, Double, Double)) = SpriteGl.clear(gl, clearColor)
    def bindTexture(loadedImage : HTMLImageElement) = SpriteGl.bindTexture(gl, loadedImage)

    def activateTexture(texture : WebGLTexture, shader : Shader) = {
        if(texture != activeTexture) {
            SpriteGl.activateTexture(gl, texture, shader.samplerUniformLocation)
            activeTexture = texture
        }
    }

    var scale = js.Array[Double](1d, 1d)
    var offset = js.Array[Double](0d, 0d)

    def resize(height : Double, centerX : Double, centerY : Double) : Unit = {
        SpriteGl.resize(gl)
        val aspectRatio = gl.canvas.clientHeight.toDouble / gl.canvas.clientWidth
        scale(1) = 2 / height
        scale(0) = scale(1) * aspectRatio
        val width = height / aspectRatio
        mutableBoundingBox.x1 = centerX - width * 0.5
        mutableBoundingBox.x2 = centerX + width * 0.5
        mutableBoundingBox.y1 = centerY - height * 0.5
        mutableBoundingBox.y2 = centerY + height * 0.5
        offset(0) = -centerX * scale(0)
        offset(1) = -centerY * scale(1)
    }

    private val setParameterUniform :((WebGLUniformLocation, Uniform[Double])) => Unit = {pair =>
        gl.uniform1f(pair._1, pair._2.value)
    }

    def drawSprites(sprites : js.Array[Sprite], from : Int, spriteCount : Int) : Unit = Measure("drawSprites"){
        if(spriteCount > vertexBuffersSpriteSize) resizeBuffers(spriteCount)

        // Blending
        val blending = sprites(from).blending
        blending.constantColor match {
            case Some((r, g, b, a)) => gl.blendColor(r, g, b, a)
            case _ =>
        }
        gl.blendEquation(blending.equation)
        gl.blendFunc(blending.sourceFactor, blending.destinationFactor)

        val shader = sprites(from).image.shader
        gl.useProgram(shader.program)
        gl.uniform2fv(shader.scaleUniformLocation, scale)
        gl.uniform2fv(shader.offsetUniformLocation, offset)
        shader.parameter1UniformLocation.foreach(setParameterUniform)

        val to = from + spriteCount // Exclusive
        var spriteIndex = from
        while(spriteIndex < to) {
            val Sprite(animation, cx, cy, w0, h, angle, _, _, _) = sprites(spriteIndex)

            animation match {
                case image : Image =>
                    activateTexture(image.stamp.texture, shader)

                    // Update coordinate data

                    {
                        val tx = image.stamp.textureLeft
                        val ty = image.stamp.textureTop
                        val tw = image.stamp.textureWidth
                        val th = image.stamp.textureHeight
                        val w = if(w0 > 0) w0 else h * image.stamp.stampWidth.toDouble / image.stamp.stampHeight
                        val x1 = (cx - w/2).toFloat
                        val y1 = (cy - h/2).toFloat
                        val x2 = (x1 + w).toFloat
                        val y2 = (y1 + h).toFloat
                        val tx1 = tx.toFloat
                        val ty1 = (ty + th).toFloat
                        val tx2 = (tx1 + tw).toFloat
                        val ty2 = ty.toFloat
                        val i = spriteIndex * vertexPerSprite * coordinatesBufferItemSize
                        val c = coordinatesBufferArray
                        c.update(i +  0, x1); c.update(i +  1, y1); c.update(i +  2, tx1); c.update(i +  3, ty1)
                        c.update(i +  4, x1); c.update(i +  5, y2); c.update(i +  6, tx1); c.update(i +  7, ty2)
                        c.update(i +  8, x2); c.update(i +  9, y2); c.update(i + 10, tx2); c.update(i + 11, ty2)
                        c.update(i + 12, x1); c.update(i + 13, y1); c.update(i + 14, tx1); c.update(i + 15, ty1)
                        c.update(i + 16, x2); c.update(i + 17, y2); c.update(i + 18, tx2); c.update(i + 19, ty2)
                        c.update(i + 20, x2); c.update(i + 21, y1); c.update(i + 22, tx2); c.update(i + 23, ty1)
                    }
                case _ =>
                    val i = spriteIndex * vertexPerSprite * coordinatesBufferItemSize
                    val c = coordinatesBufferArray
                    val w = if(w0 > 0) w0 else h
                    val x1 = (cx - w/2).toFloat
                    val y1 = (cy - h/2).toFloat
                    val x2 = (x1 + w).toFloat
                    val y2 = (y1 + h).toFloat
                    val tx1 = -1f
                    val ty1 = -1f
                    val tx2 = 1f
                    val ty2 = 1f
                    c.update(i +  0, x1); c.update(i +  1, y1); c.update(i +  2, tx1); c.update(i +  3, ty1)
                    c.update(i +  4, x1); c.update(i +  5, y2); c.update(i +  6, tx1); c.update(i +  7, ty2)
                    c.update(i +  8, x2); c.update(i +  9, y2); c.update(i + 10, tx2); c.update(i + 11, ty2)
                    c.update(i + 12, x1); c.update(i + 13, y1); c.update(i + 14, tx1); c.update(i + 15, ty1)
                    c.update(i + 16, x2); c.update(i + 17, y2); c.update(i + 18, tx2); c.update(i + 19, ty2)
                    c.update(i + 20, x2); c.update(i + 21, y1); c.update(i + 22, tx2); c.update(i + 23, ty1)
            }


            // Update rotation data
            {
                val a = angle.toFloat
                val x = cx.toFloat
                val y = cy.toFloat
                val i = spriteIndex * vertexPerSprite * rotationsBufferItemSize
                val r = rotationsBufferArray
                r.update(i +  0, x); r.update(i +  1, y); r.update(i +  2, a)
                r.update(i +  3, x); r.update(i +  4, y); r.update(i +  5, a)
                r.update(i +  6, x); r.update(i +  7, y); r.update(i +  8, a)
                r.update(i +  9, x); r.update(i + 10, y); r.update(i + 11, a)
                r.update(i + 12, x); r.update(i + 13, y); r.update(i + 14, a)
                r.update(i + 15, x); r.update(i + 16, y); r.update(i + 17, a)
            }

            spriteIndex += 1
        }

        val fromC = from * vertexPerSprite * coordinatesBufferItemSize
        val toC = to * vertexPerSprite * coordinatesBufferItemSize
        gl.bindBuffer(ARRAY_BUFFER, coordinatesBuffer)
        gl.vertexAttribPointer(shader.coordinatesAttributeLocation, coordinatesBufferItemSize, FLOAT, normalized = false, 0, 0)
        gl.enableVertexAttribArray(shader.coordinatesAttributeLocation)
        gl.bufferData(ARRAY_BUFFER, coordinatesBufferArray.subarray(fromC, toC), DYNAMIC_DRAW)

        val fromR = from * vertexPerSprite * rotationsBufferItemSize
        val toR = to * vertexPerSprite * rotationsBufferItemSize
        gl.bindBuffer(ARRAY_BUFFER, rotationBuffer)
        gl.vertexAttribPointer(shader.rotationsAttributeLocation, rotationsBufferItemSize, FLOAT, normalized = false, 0, 0)
        gl.enableVertexAttribArray(shader.rotationsAttributeLocation)
        gl.bufferData(ARRAY_BUFFER, rotationsBufferArray.subarray(fromR, toR), DYNAMIC_DRAW)

        gl.drawArrays(TRIANGLES, 0, spriteCount * vertexPerSprite)
    }

    private def resizeBuffers(spriteCount : Int): Unit = {
        coordinatesBufferArray = new Float32Array(spriteCount * vertexPerSprite * coordinatesBufferItemSize)
        rotationsBufferArray = new Float32Array(spriteCount * vertexPerSprite * rotationsBufferItemSize)
        vertexBuffersSpriteSize = spriteCount
        //println(s"vertexBuffersSpriteSize = $vertexBuffersSpriteSize")
    }

}

object SpriteGl {

    case class Shader(
        program: WebGLProgram,
        samplerUniformLocation : WebGLUniformLocation,
        scaleUniformLocation : WebGLUniformLocation,
        offsetUniformLocation : WebGLUniformLocation,
        parameter1UniformLocation : Option[(WebGLUniformLocation, Uniform[Double])],
        coordinatesAttributeLocation : Int,
        rotationsAttributeLocation : Int
    )

    def getContexts(canvas : HTMLCanvasElement) : WebGLRenderingContext = {
        val context = canvas.getContext("webgl") || canvas.getContext("experimental-webgl")
        if (context == null) throw new RuntimeException("This browser does not support WebGL.")
        context.asInstanceOf[WebGLRenderingContext]
    }

    private def initShader(gl : WebGLRenderingContext, code: String, shaderType : Int) : WebGLShader = {
        val shaderTypeName =
            if(shaderType == VERTEX_SHADER) "vertex shader"
            else if(shaderType == FRAGMENT_SHADER) "fragment shader"
            else throw new RuntimeException(s"Invalid shader type: $shaderType")

        val shader = gl.createShader(shaderType)
        gl.shaderSource(shader, code)
        gl.compileShader(shader)

        val status = gl.getShaderParameter(shader, COMPILE_STATUS).asInstanceOf[Boolean]
        if(!status) {
            val error = s"Failed to compile $shaderTypeName: ${gl.getShaderInfoLog(shader)}"
            println(error)
            println("Code")
            println(code)
            throw new RuntimeException(error)
        }

        shader
    }

    def initVertexShader(gl : WebGLRenderingContext, code: String) : WebGLShader = initShader(gl, code, VERTEX_SHADER)
    def initFragmentShader(gl : WebGLRenderingContext, code: String) : WebGLShader = initShader(gl, code, FRAGMENT_SHADER)

    def initProgram(gl : WebGLRenderingContext, vertexShader: WebGLShader, fragmentShader: WebGLShader) : WebGLProgram = {
        val shaderProgram = gl.createProgram()
        gl.attachShader(shaderProgram, vertexShader)
        gl.attachShader(shaderProgram, fragmentShader)
        gl.linkProgram(shaderProgram)
        shaderProgram
    }

    def initProgram(gl : WebGLRenderingContext, vertexShader: String, fragmentShader: String) : WebGLProgram = {
        initProgram(gl, initVertexShader(gl, vertexShader), initFragmentShader(gl, fragmentShader))
    }

    def activateTexture(gl : WebGLRenderingContext, texture : WebGLTexture, uniformLocation : WebGLUniformLocation): Unit = {
        gl.activeTexture(TEXTURE0)
        gl.bindTexture(TEXTURE_2D, texture)
        gl.uniform1i(uniformLocation, 0)
    }

    def bindTexture(gl : WebGLRenderingContext, loadedImage : HTMLImageElement) : WebGLTexture = {
        val texture = gl.createTexture()
        gl.bindTexture(TEXTURE_2D, texture)
        gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, loadedImage)
        //gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR)
        //gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR_MIPMAP_NEAREST)
        gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, NEAREST)
        gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, NEAREST)
        gl.generateMipmap(TEXTURE_2D)
        gl.bindTexture(TEXTURE_2D, null)
        texture
    }

    def clear(gl : WebGLRenderingContext, clearColor : (Double, Double, Double, Double)): Unit = {

        // Blending
        //gl.blendFunc(ONE, ONE)
        gl.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
        gl.enable(BLEND)
        gl.disable(DEPTH_TEST)

        val (r, g, b, a) = clearColor
        gl.clearColor(r, g, b, a)
        gl.clear(COLOR_BUFFER_BIT)

        // Set the view port
        gl.viewport(0, 0, gl.canvas.width, gl.canvas.height)
    }

    def resize(gl : WebGLRenderingContext) {
        // Lookup the size the browser is displaying the canvas in CSS pixels
        // and compute a size needed to make our drawingbuffer match it in
        // device pixels.
        val realToCSSPixels = dom.window.devicePixelRatio
        val displayWidth = Math.floor(gl.canvas.clientWidth * realToCSSPixels).toInt
        val displayHeight = Math.floor(gl.canvas.clientHeight * realToCSSPixels).toInt

        if (gl.canvas.width != displayWidth || gl.canvas.height != displayHeight) {
            gl.canvas.width = displayWidth
            gl.canvas.height = displayHeight
        }
    }
}

