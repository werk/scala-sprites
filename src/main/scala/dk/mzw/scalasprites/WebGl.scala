package dk.mzw.scalasprites

import org.scalajs.dom
import org.scalajs.dom.raw.{Event, HTMLCanvasElement, HTMLImageElement, WebGLBuffer, WebGLProgram, WebGLShader, WebGLTexture, WebGLUniformLocation, WebGLRenderingContext => GL}

import scala.collection.immutable.{::, Iterable, Nil}
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class WebGl(canvas : HTMLCanvasElement) {

    var coordinatesAttributeLocation : Int = 0
    var textureCoordinatesAttributeLocation : Int = 0
    var textureCoordinatesBuffer: WebGLBuffer = null
    var samplerUniformLocation : WebGLUniformLocation = null

    private val gl = {
        val context = canvas.getContext("webgl") || canvas.getContext("experimental-webgl")
        if (context == null) throw new RuntimeException("This browser does not support WebGL.")
        context.asInstanceOf[GL]
    }
    private val realToCSSPixels = dom.window.devicePixelRatio

    def initVertexShader(code: String) : WebGLShader = {
        val shader = gl.createShader(GL.VERTEX_SHADER)
        gl.shaderSource(shader, code)
        gl.compileShader(shader)
        shader
    }

    def initFragmentShader(code: String) : WebGLShader = {
        val shader = gl.createShader(GL.FRAGMENT_SHADER)
        gl.shaderSource(shader, code)
        gl.compileShader(shader)
        shader
    }

    def initProgram(vertexShader: WebGLShader, fragmentShader: WebGLShader): WebGLProgram = {
        val shaderProgram = gl.createProgram()
        gl.attachShader(shaderProgram, vertexShader)
        gl.attachShader(shaderProgram, fragmentShader)
        gl.linkProgram(shaderProgram)
        shaderProgram
    }

    def initProgram(vertexShader: String, fragmentShader: String) : WebGLProgram = {
        initProgram(initVertexShader(vertexShader), initFragmentShader(fragmentShader))
    }

    def initSpriteProgram() : Unit = {
        val vertexCode = """
            attribute vec2 coordinates;
            attribute vec2 a_textureCoordinates;

            varying vec2 v_textureCoordinates;

            void main(void) {
                gl_Position = vec4(coordinates, 1.0, 1.0);
                v_textureCoordinates = a_textureCoordinates;
            }
        """

        val fragmentCode = """
            uniform sampler2D uSampler;
            varying vec2 v_textureCoordinates;

            void main(void) {
              gl_FragColor = texture2D(uSampler, v_textureCoordinates);
            }
        """

        val program = initProgram(vertexCode, fragmentCode)
        gl.useProgram(program)

        coordinatesAttributeLocation = gl.getAttribLocation(program, "coordinates")
        textureCoordinatesAttributeLocation = gl.getAttribLocation(program, "a_textureCoordinates")
        samplerUniformLocation = gl.getUniformLocation(program, "uSampler")

        // Create a buffer for texture coordinates
        textureCoordinatesBuffer = gl.createBuffer()
        gl.bindBuffer(GL.ARRAY_BUFFER, textureCoordinatesBuffer)

        // Put texcoords in the buffer
        val textureCoordinates = js.Array(
            0, 0,
            0, 1,
            1, 1,
            0, 0,
            1, 1,
            1, 0
        )
        gl.bufferData(GL.ARRAY_BUFFER, new Float32Array(textureCoordinates), GL.STATIC_DRAW)
    }

    def initPointSpriteProgram() : Unit = {
        val vertexCode = """
            attribute vec3 coordinates;

            void main(void) {
                gl_Position = vec4(coordinates.xy, 1.0, 1.0);
                gl_PointSize = coordinates.z;
            }
        """

        val fragmentCode = """
            uniform sampler2D uSampler;

            void main(void) {
              gl_FragColor = texture2D(uSampler, gl_PointCoord);
            }
        """

        val program = initProgram(vertexCode, fragmentCode)
        gl.useProgram(program)

        coordinatesAttributeLocation = gl.getAttribLocation(program, "coordinates")
        samplerUniformLocation = gl.getUniformLocation(program, "uSampler")
    }

    def activateTexture(texture : WebGLTexture): Unit = {
        gl.activeTexture(GL.TEXTURE0)
        gl.bindTexture(GL.TEXTURE_2D, texture)
        gl.uniform1i(samplerUniformLocation, 0)
    }

    def initTexture(url : String, onLoad : WebGLTexture => Unit) {
        val texture = gl.createTexture()
        val image = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
        image.onload = { _ : Event =>
            println(s"image.onload $url")
            gl.bindTexture(GL.TEXTURE_2D, texture)
            gl.texImage2D(GL.TEXTURE_2D, 0, GL.RGBA, GL.RGBA, GL.UNSIGNED_BYTE, image)
            gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MAG_FILTER, GL.LINEAR)
            gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.LINEAR_MIPMAP_NEAREST)
            gl.generateMipmap(GL.TEXTURE_2D)
            gl.bindTexture(GL.TEXTURE_2D, null)
            onLoad(texture)
        }
        image.src = url
    }

    def initTextures(urls : Iterable[String], onLoad : Map[String, WebGLTexture] => Unit) : Unit = {
        initTextures(urls.toList.distinct, onLoad, Map())
    }

    private def initTextures(urls : List[String], onLoad : Map[String, WebGLTexture] => Unit, map : Map[String, WebGLTexture]) : Unit = urls match {
        case List() => onLoad(map)
        case url :: rest =>
            initTexture(url, {texture =>
                initTextures(rest, onLoad, map + (url -> texture))
            })
    }

    def clear(): Unit = {
        // Clear the canvas
        gl.clearColor(0.3, 0.3, 0.3, 1)

        // Enable the depth test
        //gl.enable(gl.DEPTH_TEST);

        // Blending
        gl.blendFunc(GL.ONE, GL.ONE)
        gl.enable(GL.BLEND)
        gl.disable(GL.DEPTH_TEST)

        // Clear the color buffer bit
        gl.clear(GL.COLOR_BUFFER_BIT)

        // Set the view port
        gl.viewport(0, 0, canvas.width, canvas.height)

    }
    /*
    def drawPointSprites(points : Array[(Double, Double, Double)]) {
        resize()

        /*==========Defining and storing the geometry=======*/

        val vertices = js.Array(points.flatMap{case (x, y, s) => Array(x, y, s)} : _*)

        // Create an empty buffer object to store the vertex buffer
        val vertexBuffer = gl.createBuffer()

        //Bind appropriate array buffer to it
        gl.bindBuffer(GL.ARRAY_BUFFER, vertexBuffer)

        // Pass the vertex data to the buffer
        gl.bufferData(GL.ARRAY_BUFFER, new Float32Array(vertices), GL.STREAM_DRAW)

        // Unbind the buffer
        gl.bindBuffer(GL.ARRAY_BUFFER, null)

        /*======== Associating shaders to buffer objects ========*/

        // Bind vertex buffer object
        gl.bindBuffer(GL.ARRAY_BUFFER, vertexBuffer)


        // Point an attribute to the currently bound VBO
        gl.vertexAttribPointer(coordinatesAttributeLocation, 3, GL.FLOAT, normalized = false, 0, 0)

        // Enable the attribute
        gl.enableVertexAttribArray(coordinatesAttributeLocation)

        /*============= Drawing the primitive ===============*/

        gl.drawArrays(GL.POINTS, 0, points.length)
    }
    */

    def drawSprites(points : Array[(Double, Double, Double, Double)]) {
        resize()

        /*==========Defining and storing the geometry=======*/

        val vertices = js.Array(points.flatMap{case (x, y, w, h) =>
            Array(
                x, y,
                x, y + h,
                x + w, y + h,
                x, y,
                x + w, y + h,
                x + w, y
            )
        } : _*)

        // Create an empty buffer object to store the vertex buffer
        val vertexBuffer = gl.createBuffer()

        //Bind appropriate array buffer to it
        gl.bindBuffer(GL.ARRAY_BUFFER, vertexBuffer)

        // Pass the vertex data to the buffer
        gl.bufferData(GL.ARRAY_BUFFER, new Float32Array(vertices), GL.STREAM_DRAW)

        // Unbind the buffer
        gl.bindBuffer(GL.ARRAY_BUFFER, null)

        /*======== Associating shaders to buffer objects ========*/

        // Bind vertex buffer object
        gl.bindBuffer(GL.ARRAY_BUFFER, vertexBuffer)


        // Point an attribute to the currently bound VBO
        gl.vertexAttribPointer(coordinatesAttributeLocation, 2, GL.FLOAT, normalized = false, 0, 0)

        // Enable the attribute
        gl.enableVertexAttribArray(coordinatesAttributeLocation)

        // Texture coordinates
        gl.bindBuffer(GL.ARRAY_BUFFER, textureCoordinatesBuffer)
        gl.enableVertexAttribArray(textureCoordinatesAttributeLocation)
        gl.vertexAttribPointer(textureCoordinatesAttributeLocation, 2, GL.FLOAT, normalized = false, 0, 0)

        /*============= Drawing the primitive ===============*/

        gl.drawArrays(GL.TRIANGLES, 0, points.length * 6)
    }

    def resize() {
        // Lookup the size the browser is displaying the canvas in CSS pixels
        // and compute a size needed to make our drawingbuffer match it in
        // device pixels.
        val displayWidth = Math.floor(gl.canvas.clientWidth * realToCSSPixels).toInt
        val displayHeight = Math.floor(gl.canvas.clientHeight * realToCSSPixels).toInt

        if (gl.canvas.width != displayWidth || gl.canvas.height != displayHeight) {
            gl.canvas.width = displayWidth
            gl.canvas.height = displayHeight
        }
    }
}
