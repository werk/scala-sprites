package dk.mzw.scalasprites.gl

import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture, WebGLUniformLocation, WebGLRenderingContext => GL}
import scala.collection.immutable.Iterable

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class QuadGl(canvas : HTMLCanvasElement) {

    var coordinatesAttributeLocation : Int = _
    var samplerUniformLocation : WebGLUniformLocation = _
    var scaleUniformLocation : WebGLUniformLocation = _
    var offsetUniformLocation : WebGLUniformLocation = _

    val gl = WebGl.getContexts(canvas)

    def initSpriteProgram() : Unit = {
        val vertexCode = """
            attribute vec4 a_coordinates;
            uniform vec2 u_scale;
            uniform vec2 u_offset;
            //attribute vec2 a_textureCoordinates;

            varying vec2 v_textureCoordinates;

            void main(void) {
                gl_Position = vec4(a_coordinates.xy * u_scale + u_offset, 1.0, 1.0);
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

        val program = WebGl.initProgram(gl, vertexCode, fragmentCode)
        gl.useProgram(program)

        coordinatesAttributeLocation = gl.getAttribLocation(program, "a_coordinates")
        samplerUniformLocation = gl.getUniformLocation(program, "u_sampler")
        scaleUniformLocation = gl.getUniformLocation(program, "u_scale")
        offsetUniformLocation = gl.getUniformLocation(program, "u_offset")
    }

    def clear() = WebGl.clear(gl)
    def initTextures(urls : Iterable[String], onLoad : Map[String, WebGLTexture] => Unit) : Unit = WebGl.initTextures(gl, urls, onLoad)
    def activateTexture(texture : WebGLTexture) = WebGl.activateTexture(gl, texture, samplerUniformLocation)

    def drawSprites(height : Double, points : Array[(Double, Double, Double, Double)]) {
        WebGl.resize(gl)
        val aspectRatio = gl.canvas.clientHeight.toDouble / gl.canvas.clientWidth
        val scaleY = 2 / height
        val scaleX = scaleY * aspectRatio


        gl.uniform2fv(scaleUniformLocation, js.Array[Double](scaleX, scaleY))
        gl.uniform2fv(offsetUniformLocation, js.Array[Double](0, 0))

        /*==========Defining and storing the geometry=======*/

        val vertices = js.Array(points.flatMap{case (cx, cy, w, h) =>
            val x = cx - w/2
            val y = cy - h/2
            Array(
                x, y, 0, 0,
                x, y + h, 0, 1,
                x + w, y + h, 1, 1,
                x, y, 0, 0,
                x + w, y + h, 1, 1,
                x + w, y, 1, 0
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
        gl.vertexAttribPointer(coordinatesAttributeLocation, 4, GL.FLOAT, normalized = false, 0, 0)

        // Enable the attribute
        gl.enableVertexAttribArray(coordinatesAttributeLocation)

        /*============= Drawing the primitive ===============*/

        gl.drawArrays(GL.TRIANGLES, 0, points.length * 6)
    }

}
