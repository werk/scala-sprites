package dk.mzw.scalasprites

import dk.mzw.scalasprites.SpriteGl.Shape
import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{HTMLCanvasElement, HTMLImageElement, WebGLProgram, WebGLRenderingContext, WebGLShader, WebGLTexture, WebGLUniformLocation}

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

    def clear() = SpriteGl.clear(gl)
    def bindTexture(loadedImage : HTMLImageElement) = SpriteGl.bindTexture(gl, loadedImage)
    def activateTexture(texture : WebGLTexture) = SpriteGl.activateTexture(gl, texture, samplerUniformLocation)

    def drawSprites(height : Double, points : Array[Shape]) {
        SpriteGl.resize(gl)
        val aspectRatio = gl.canvas.clientHeight.toDouble / gl.canvas.clientWidth
        val scaleY = 2 / height
        val scaleX = scaleY * aspectRatio


        gl.uniform2fv(scaleUniformLocation, js.Array[Double](scaleX, scaleY))
        gl.uniform2fv(offsetUniformLocation, js.Array[Double](0, 0))

        /*==========Defining and storing the geometry=======*/

        val coordinates = js.Array(points.flatMap{case Shape(cx, cy, w, h, _, tx, ty, tw, th) =>
            val x1 = cx - w/2
            val y1 = cy - h/2
            val x2 = x1 + w
            val y2 = y1 + h
            val tx1 = tx
            val ty1 = ty + th
            val tx2 = tx1 + tw
            val ty2 = ty
            Array(
                x1, y1, tx1, ty1,
                x1, y2, tx1, ty2,
                x2, y2, tx2, ty2,
                x1, y1, tx1, ty1,
                x2, y2, tx2, ty2,
                x2, y1, tx2, ty1
            )
        } : _*)

        val rotations = js.Array(points.flatMap{case Shape(cx, cy, _, _, a, _, _, _, _) =>
            Array(
                cx, cy, a,
                cx, cy, a,
                cx, cy, a,
                cx, cy, a,
                cx, cy, a,
                cx, cy, a
            )
        } : _*)

        val coordinatesBuffer = gl.createBuffer()
        gl.bindBuffer(ARRAY_BUFFER, coordinatesBuffer)
        gl.bufferData(ARRAY_BUFFER, new Float32Array(coordinates), STREAM_DRAW)
        gl.vertexAttribPointer(coordinatesAttributeLocation, 4, FLOAT, normalized = false, 0, 0)
        gl.enableVertexAttribArray(coordinatesAttributeLocation)

        val rotationBuffer = gl.createBuffer()
        gl.bindBuffer(ARRAY_BUFFER, rotationBuffer)
        gl.bufferData(ARRAY_BUFFER, new Float32Array(rotations), STREAM_DRAW)
        gl.vertexAttribPointer(rotationsAttributeLocation, 3, FLOAT, normalized = false, 0, 0)
        gl.enableVertexAttribArray(rotationsAttributeLocation)

        gl.drawArrays(TRIANGLES, 0, points.length * 6)
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

    def clear(gl : WebGLRenderingContext): Unit = {
        // Clear the canvas
        gl.clearColor(0.3, 0.3, 0.3, 1)

        // Enable the depth test
        //gl.enable(gl.DEPTH_TEST);

        // Blending
        gl.blendFunc(ONE, ONE)
        gl.enable(BLEND)
        gl.disable(DEPTH_TEST)

        // Clear the color buffer bit
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

