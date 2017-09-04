package dk.mzw.scalasprites.gl

import dk.mzw.scalasprites.gl.WebGl.LoadedTexture
import org.scalajs.dom
import org.scalajs.dom.raw.{Event, HTMLCanvasElement, HTMLImageElement, WebGLProgram, WebGLShader, WebGLTexture, WebGLUniformLocation, WebGLRenderingContext => GL}

import scala.collection.immutable.{::, Iterable}
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class PointSpriteGl(canvas : HTMLCanvasElement) {

    var coordinatesAttributeLocation : Int = 0
    var samplerUniformLocation : WebGLUniformLocation = _

    val gl = WebGl.getContexts(canvas)

    def initSpriteProgram() : Unit = {
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

        val program = WebGl.initProgram(gl, vertexCode, fragmentCode)
        gl.useProgram(program)

        coordinatesAttributeLocation = gl.getAttribLocation(program, "coordinates")
        samplerUniformLocation = gl.getUniformLocation(program, "uSampler")
    }

    def clear() = WebGl.clear(gl)
    def initTextures(urls : Iterable[String], onLoad : Map[String, LoadedTexture] => Unit) : Unit = WebGl.initTextures(gl, urls, onLoad)
    def activateTexture(texture : WebGLTexture) = WebGl.activateTexture(gl, texture, samplerUniformLocation)

    def drawSprites(points : Array[(Double, Double, Double, Double)]) {
        WebGl.resize(gl)

        /*==========Defining and storing the geometry=======*/

        val vertices = js.Array(points.flatMap{case (x, y, s, _) => Array(x, y, s)} : _*)

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
}
