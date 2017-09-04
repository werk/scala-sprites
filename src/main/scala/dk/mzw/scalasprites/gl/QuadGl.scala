package dk.mzw.scalasprites.gl

import dk.mzw.scalasprites.gl.WebGl.{LoadedTexture, Shape}
import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLTexture, WebGLUniformLocation, WebGLRenderingContext => GL}

import scala.collection.immutable.Iterable
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class QuadGl(canvas : HTMLCanvasElement) {

    var coordinatesAttributeLocation : Int = _
    var rotationsAttributeLocation : Int = _
    var samplerUniformLocation : WebGLUniformLocation = _
    var scaleUniformLocation : WebGLUniformLocation = _
    var offsetUniformLocation : WebGLUniformLocation = _

    val gl = WebGl.getContexts(canvas)

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

        val program = WebGl.initProgram(gl, vertexCode, fragmentCode)
        gl.useProgram(program)

        coordinatesAttributeLocation = gl.getAttribLocation(program, "a_coordinates")
        rotationsAttributeLocation = gl.getAttribLocation(program, "a_rotation")
        samplerUniformLocation = gl.getUniformLocation(program, "u_sampler")
        scaleUniformLocation = gl.getUniformLocation(program, "u_scale")
        offsetUniformLocation = gl.getUniformLocation(program, "u_offset")
    }

    def clear() = WebGl.clear(gl)
    def initTextures(urls : Iterable[String], onLoad : Map[String, LoadedTexture] => Unit) : Unit = WebGl.initTextures(gl, urls, onLoad)
    def activateTexture(texture : WebGLTexture) = WebGl.activateTexture(gl, texture, samplerUniformLocation)

    def drawSprites(height : Double, points : Array[Shape]) {
        WebGl.resize(gl)
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
        gl.bindBuffer(GL.ARRAY_BUFFER, coordinatesBuffer)
        gl.bufferData(GL.ARRAY_BUFFER, new Float32Array(coordinates), GL.STREAM_DRAW)
        gl.bindBuffer(GL.ARRAY_BUFFER, null)

        val rotationBuffer = gl.createBuffer()
        gl.bindBuffer(GL.ARRAY_BUFFER, rotationBuffer)
        gl.bufferData(GL.ARRAY_BUFFER, new Float32Array(rotations), GL.STREAM_DRAW)
        gl.bindBuffer(GL.ARRAY_BUFFER, null)

        /*======== Associating shaders to buffer objects ========*/

        gl.bindBuffer(GL.ARRAY_BUFFER, coordinatesBuffer)
        gl.vertexAttribPointer(coordinatesAttributeLocation, 4, GL.FLOAT, normalized = false, 0, 0)
        gl.enableVertexAttribArray(coordinatesAttributeLocation)

        gl.bindBuffer(GL.ARRAY_BUFFER, rotationBuffer)
        gl.vertexAttribPointer(rotationsAttributeLocation, 3, GL.FLOAT, normalized = false, 0, 0)
        gl.enableVertexAttribArray(rotationsAttributeLocation)

        /*============= Drawing the primitive ===============*/

        gl.drawArrays(GL.TRIANGLES, 0, points.length * 6)
    }

}
