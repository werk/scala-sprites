package dk.mzw.scalasprites.gl

import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture, WebGLUniformLocation, WebGLRenderingContext => GL}
import scala.collection.immutable.Iterable

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class QuadGl(canvas : HTMLCanvasElement) {

    var coordinatesAttributeLocation : Int = _
    var extraAttributeLocation : Int = _
    var samplerUniformLocation : WebGLUniformLocation = _
    var scaleUniformLocation : WebGLUniformLocation = _
    var offsetUniformLocation : WebGLUniformLocation = _

    val gl = WebGl.getContexts(canvas)

    def initSpriteProgram() : Unit = {
        val vertexCode = """
            attribute vec4 a_coordinates;
            attribute vec2 a_extra;
            uniform vec2 u_scale;
            uniform vec2 u_offset;

            varying vec2 v_textureCoordinates;

            void main(void) {
                float b = a_coordinates.z;
                float tx = float(b >= 2.0);
                float ty = float(b == 1.0 || b == 3.0);
                float angle = a_coordinates.w;
                float s = sin(angle);
                float c = cos(angle);
                mat2 rotate = mat2(c, s, -s, c);
                vec2 p = rotate * (a_coordinates.xy - a_extra) + a_extra;
                gl_Position = vec4(p * u_scale + u_offset, 1.0, 1.0);
                v_textureCoordinates = vec2(tx, ty);
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
        extraAttributeLocation = gl.getAttribLocation(program, "a_extra")
        samplerUniformLocation = gl.getUniformLocation(program, "u_sampler")
        scaleUniformLocation = gl.getUniformLocation(program, "u_scale")
        offsetUniformLocation = gl.getUniformLocation(program, "u_offset")
    }

    def clear() = WebGl.clear(gl)
    def initTextures(urls : Iterable[String], onLoad : Map[String, WebGLTexture] => Unit) : Unit = WebGl.initTextures(gl, urls, onLoad)
    def activateTexture(texture : WebGLTexture) = WebGl.activateTexture(gl, texture, samplerUniformLocation)

    def drawSprites(height : Double, points : Array[(Double, Double, Double, Double, Double)]) {
        WebGl.resize(gl)
        val aspectRatio = gl.canvas.clientHeight.toDouble / gl.canvas.clientWidth
        val scaleY = 2 / height
        val scaleX = scaleY * aspectRatio


        gl.uniform2fv(scaleUniformLocation, js.Array[Double](scaleX, scaleY))
        gl.uniform2fv(offsetUniformLocation, js.Array[Double](0, 0))

        /*==========Defining and storing the geometry=======*/

        val vertices = js.Array(points.flatMap{case (cx, cy, w, h, a) =>
            val x = cx - w/2
            val y = cy - h/2
            Array(
                x, y, 1, a,
                x, y + h, 0, a,
                x + w, y + h, 2, a,
                x, y, 1, a,
                x + w, y + h, 2, a,
                x + w, y, 3, a
            )
        } : _*)

        val verticesExtra = js.Array(points.flatMap{case (cx, cy, _, _, _) =>
            Array(
                cx, cy,
                cx, cy,
                cx, cy,
                cx, cy,
                cx, cy,
                cx, cy
            )
        } : _*)

        val vertexBuffer = gl.createBuffer()
        gl.bindBuffer(GL.ARRAY_BUFFER, vertexBuffer)
        gl.bufferData(GL.ARRAY_BUFFER, new Float32Array(vertices), GL.STREAM_DRAW)
        gl.bindBuffer(GL.ARRAY_BUFFER, null)

        val vertexExtraBuffer = gl.createBuffer()
        gl.bindBuffer(GL.ARRAY_BUFFER, vertexExtraBuffer)
        gl.bufferData(GL.ARRAY_BUFFER, new Float32Array(verticesExtra), GL.STREAM_DRAW)
        gl.bindBuffer(GL.ARRAY_BUFFER, null)

        /*======== Associating shaders to buffer objects ========*/

        gl.bindBuffer(GL.ARRAY_BUFFER, vertexBuffer)
        gl.vertexAttribPointer(coordinatesAttributeLocation, 4, GL.FLOAT, normalized = false, 0, 0)
        gl.enableVertexAttribArray(coordinatesAttributeLocation)

        gl.bindBuffer(GL.ARRAY_BUFFER, vertexExtraBuffer)
        gl.vertexAttribPointer(extraAttributeLocation, 2, GL.FLOAT, normalized = false, 0, 0)
        gl.enableVertexAttribArray(extraAttributeLocation)

        /*============= Drawing the primitive ===============*/

        gl.drawArrays(GL.TRIANGLES, 0, points.length * 6)
    }

}
