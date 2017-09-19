package dk.mzw.scalasprites

import dk.mzw.scalasprites.SpriteCanvas.Sprite
import dk.mzw.scalasprites.SpriteGl.Shape
import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw._

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class SpriteGl(canvas : HTMLCanvasElement) {

    var coordinatesAttributeLocation : Int = _
    var rotationsAttributeLocation : Int = _
    var samplerUniformLocation : WebGLUniformLocation = _
    var scaleUniformLocation : WebGLUniformLocation = _
    var offsetUniformLocation : WebGLUniformLocation = _

    val gl = SpriteGl.getContexts(canvas)

    def initSpriteProgram() : Unit = {
        val vertexCode = """
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

        val fragmentCode = """
            precision highp float;

            uniform sampler2D u_sampler;
            varying vec2 v_textureCoordinates;

            void main(void) {
              gl_FragColor = texture2D(u_sampler, v_textureCoordinates);
            }
        """

        val program = SpriteGl.initProgram(gl, vertexCode, fragmentCode)
        gl.useProgram(program)

        coordinatesAttributeLocation = gl.getAttribLocation(program, "a_coordinates")
        rotationsAttributeLocation = gl.getAttribLocation(program, "a_rotation")
        samplerUniformLocation = gl.getUniformLocation(program, "u_sampler")
        scaleUniformLocation = gl.getUniformLocation(program, "u_scale")
        offsetUniformLocation = gl.getUniformLocation(program, "u_offset")
    }

    def clear(clearColor : (Double, Double, Double, Double)) = SpriteGl.clear(gl, clearColor)
    def bindTexture(loadedImage : HTMLImageElement) = SpriteGl.bindTexture(gl, loadedImage)

    private var activeTexture : WebGLTexture = null
    def activateTexture(texture : WebGLTexture) = {
        if(texture != activeTexture) {
            SpriteGl.activateTexture(gl, texture, samplerUniformLocation)
            activeTexture = texture
        }
    }


    val vertexPerSprite = 6
    var maxSpriteCount = 100

    val coordinatesBuffer : WebGLBuffer = gl.createBuffer()
    gl.bindBuffer(ARRAY_BUFFER, coordinatesBuffer)
    gl.vertexAttribPointer(coordinatesAttributeLocation, 4, FLOAT, normalized = false, 0, 0)
    gl.enableVertexAttribArray(coordinatesAttributeLocation)
    val coordinatesBufferItemSize = 4
    var coordinatesBufferArray : Float32Array = _

    val rotationBuffer : WebGLBuffer = gl.createBuffer()
    val rotationsBufferItemSize = 3
    var rotationsBufferArray : Float32Array = _

    resizeBuffers(maxSpriteCount)

    def resizeBuffers(spriteCount : Int): Unit = {
        coordinatesBufferArray = new Float32Array(spriteCount * vertexPerSprite * coordinatesBufferItemSize)
        rotationsBufferArray = new Float32Array(spriteCount * vertexPerSprite * rotationsBufferItemSize)
        maxSpriteCount = spriteCount
        println(s"maxSpriteCount = $maxSpriteCount")
    }

    def drawSprites(sprites : js.Array[Sprite], from : Int, to : Int, height : Double, centerX : Double, centerY : Double) {
        val spriteCount = to - from + 1
        //println(s"drawSprites from=$from, to=$to, count=$spriteCount, sprites.length=${sprites.length}")
        SpriteGl.resize(gl)
        val aspectRatio = gl.canvas.clientHeight.toDouble / gl.canvas.clientWidth
        val scaleY = 2 / height
        val scaleX = scaleY * aspectRatio

        gl.uniform2fv(scaleUniformLocation, js.Array[Double](scaleX, scaleY))
        gl.uniform2fv(offsetUniformLocation, js.Array[Double](-centerX * scaleX, -centerY * scaleY))

        /*==========Defining and storing the geometry=======*/

        if(spriteCount > maxSpriteCount) resizeBuffers(spriteCount)

        for(spriteIndex <- from to to) {
            val Sprite(image, cx, cy, h, angle, _, _, _) = sprites(spriteIndex)
            activateTexture(image.stamp.texture)

            // Update coordinate data
            {
                val tx = image.stamp.textureLeft
                val ty = image.stamp.textureTop
                val tw = image.stamp.textureWidth
                val th = image.stamp.textureHeight
                val w = h * image.stamp.stampWidth.toDouble / image.stamp.stampHeight
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

            // Update rotation data
            {
                val a = angle.toFloat
                val x = cx.toFloat
                val y = cy.toFloat
                val i = spriteIndex * vertexPerSprite * coordinatesBufferItemSize
                val r = rotationsBufferArray
                r.update(i +  0, x); r.update(i +  1, y); r.update(i +  2, a)
                r.update(i +  3, x); r.update(i +  4, y); r.update(i +  5, a)
                r.update(i +  6, x); r.update(i +  7, y); r.update(i +  8, a)
                r.update(i +  9, x); r.update(i + 10, y); r.update(i + 11, a)
                r.update(i + 12, x); r.update(i + 13, y); r.update(i + 14, a)
                r.update(i + 15, x); r.update(i + 16, y); r.update(i + 17, a)
            }
        }

        val fromC = from * vertexPerSprite * coordinatesBufferItemSize
        val toC = (to + 1) * vertexPerSprite * coordinatesBufferItemSize
        val fromR = from * vertexPerSprite * rotationsBufferItemSize
        val toR = (to + 1) * vertexPerSprite * rotationsBufferItemSize

        gl.bindBuffer(ARRAY_BUFFER, coordinatesBuffer)
        gl.bufferData(ARRAY_BUFFER, coordinatesBufferArray.subarray(fromC, toC), DYNAMIC_DRAW)

        gl.bindBuffer(ARRAY_BUFFER, rotationBuffer)
        gl.bufferData(ARRAY_BUFFER, rotationsBufferArray.subarray(fromR, toR), DYNAMIC_DRAW)

        gl.drawArrays(TRIANGLES, 0, spriteCount * vertexPerSprite)
    }
}

object SpriteGl {

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
        gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR)
        gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR_MIPMAP_NEAREST)
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

    case class Shape(
        cx : Double,
        cy : Double,
        w : Double,
        h : Double,
        a : Double,
        tx : Double,
        ty : Double,
        tw : Double,
        th : Double
    )

}

