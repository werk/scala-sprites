package dk.mzw.scalasprites.gl

import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture, WebGLUniformLocation, WebGLRenderingContext => GL}
import scala.collection.immutable.Iterable

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class QuadGl(canvas : HTMLCanvasElement) {

    var coordinatesAttributeLocation : Int = 0
    //var textureCoordinatesAttributeLocation : Int = 0
    //var textureCoordinatesBuffer: WebGLBuffer = null
    var samplerUniformLocation : WebGLUniformLocation = null

    val gl = WebGl.getContexts(canvas)


    def initSpriteProgram() : Unit = {
        val vertexCode = """
            attribute vec2 coordinates;
            //attribute vec2 a_textureCoordinates;

            //varying vec2 v_textureCoordinates;

            void main(void) {
                gl_Position = vec4(coordinates, 1.0, 1.0);
                //v_textureCoordinates = a_textureCoordinates;
            }
        """

        val fragmentCode = """
            precision highp float;

            uniform sampler2D uSampler;
            //varying vec2 v_textureCoordinates;

            void main(void) {
              gl_FragColor = vec4(1.0, 0.0, 0.5, 1.0); // texture2D(uSampler, v_textureCoordinates);
            }
        """

        val program = WebGl.initProgram(gl, vertexCode, fragmentCode)
        gl.useProgram(program)

        coordinatesAttributeLocation = gl.getAttribLocation(program, "coordinates")
        //textureCoordinatesAttributeLocation = gl.getAttribLocation(program, "a_textureCoordinates")
        samplerUniformLocation = gl.getUniformLocation(program, "uSampler")

        /*
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
        gl.bindBuffer(GL.ARRAY_BUFFER, null)
        */
    }

    def clear() = WebGl.clear(gl)
    def initTextures(urls : Iterable[String], onLoad : Map[String, WebGLTexture] => Unit) : Unit = WebGl.initTextures(gl, urls, onLoad)
    def activateTexture(texture : WebGLTexture) = WebGl.activateTexture(gl, texture, samplerUniformLocation)

    def drawSprites(points : Array[(Double, Double, Double, Double)]) {
        WebGl.resize(gl)

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
        //gl.bindBuffer(GL.ARRAY_BUFFER, textureCoordinatesBuffer)
        //gl.enableVertexAttribArray(textureCoordinatesAttributeLocation)
        //gl.vertexAttribPointer(textureCoordinatesAttributeLocation, 2, GL.FLOAT, normalized = false, 0, 0)

        /*============= Drawing the primitive ===============*/

        gl.drawArrays(GL.TRIANGLES, 0, points.length * 6)
    }

}
