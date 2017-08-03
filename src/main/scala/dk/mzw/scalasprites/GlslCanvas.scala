package dk.mzw.scalasprites

import dk.mzw.accelemation.internal.Internal.Uniform
import org.scalajs.dom.raw.{HTMLCanvasElement, WebGLRenderingContext, WebGLShader, WebGLUniformLocation}

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object GlslCanvas {
    private val vertexShaderSource =
        "precision highp float;\n" +
            "attribute vec2 a_position;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = vec4(a_position, 0, 1);\n" +
            "}\n"
}
/*
class GlslCanvas(
    canvas : HTMLCanvasElement,
    uniforms : List[Uniform[_]],
    fragmentShaderSource : String,
    vertexShaderSource : String = GlslCanvas.vertexShaderSource
) {

    val (gl, uniformLocations) = setupContext()

    private def setupContext() : (WebGLRenderingContext, Map[Uniform[_], WebGLUniformLocation]) = {
        val context = canvas.getContext("webgl") || canvas.getContext("experimental-webgl")
        if(context == null) throw new RuntimeException("This browser does not support WebGL.")
        val gl = context.asInstanceOf[WebGLRenderingContext]

        val program = gl.createProgram()
        gl.attachShader(program, loadShader(gl, GlslCanvas.vertexShaderSource, WebGLRenderingContext.VERTEX_SHADER))
        gl.attachShader(program, loadShader(gl, fragmentShaderSource, WebGLRenderingContext.FRAGMENT_SHADER))
        gl.linkProgram(program)
        if(gl.getProgramParameter(program, WebGLRenderingContext.LINK_STATUS) == null) {
            val lastError = gl.getProgramInfoLog(program)
            throw new RuntimeException("Error in program linking: " + lastError)
        }

        gl.useProgram(program)

        // TODO fix name clash
        val uniformLocations = uniforms.map{u =>
            u -> gl.getUniformLocation(program, u.name)
        }.toMap

        val positionLocation = gl.getAttribLocation(program, "a_position")

        val buffer = gl.createBuffer()
        gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffer)
        gl.enableVertexAttribArray(positionLocation)
        gl.vertexAttribPointer(
            indx = positionLocation,
            size = 2,
            `type` = WebGLRenderingContext.FLOAT,
            normalized = false,
            stride = 0, offset = 0
        )
        gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, new Float32Array(js.Array(
            -1, -1,   -1, +1,   +1, +1,
            -1, -1,   +1, +1,   +1, -1
        )), WebGLRenderingContext.STATIC_DRAW)

        // TODO
        //gl.uniform2f(my.uniforms.u_scale, 1, 1);
        //gl.uniform2f(my.uniforms.u_offset, 0, 0);
        //gl.uniform1f(my.uniforms.u_time, 0);

        (gl, uniformLocations)
    }


    private def loadShader(gl : WebGLRenderingContext, shaderSource : String, shaderType : Int) : WebGLShader = {
        val shader = gl.createShader(shaderType)
        gl.shaderSource(shader, shaderSource)
        gl.compileShader(shader)
        val compileStatus = gl.getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS)
        println(s"compileStatus: $compileStatus")
        println(shaderSource)
        if(!compileStatus.asInstanceOf[Boolean]) {
            val lastError = gl.getShaderInfoLog(shader)
            gl.deleteShader(shader)
            throw new RuntimeException(s"Error compiling shader '$shader': $lastError")
        }
        shader
    }

    private def b2i(b : Boolean) = if(b) 1 else 0

    private def setUniform(u : Uniform[_]): Unit = {
        val location = uniformLocations(u)
        val vector = u.value
        vector match {
            case x : Double => gl.uniform1f(location, x);
            case (x : Double, y : Double) => gl.uniform2f(location, x, y)
            case (x : Double, y : Double, z : Double) => gl.uniform3f(location, x, y, z)
            case (x : Double, y : Double, z : Double, w : Double) => gl.uniform4f(location, x, y, z, w)
            
            case x : Int => gl.uniform1i(location, x);
            case (x : Int, y : Int) => gl.uniform2i(location, x, y)
            case (x : Int, y : Int, z : Int) => gl.uniform3i(location, x, y, z)
            case (x : Int, y : Int, z : Int, w : Int) => gl.uniform4i(location, x, y, z, w)

            case x : Boolean => gl.uniform1i(location, b2i(x));
            case (x : Boolean, y : Boolean) => gl.uniform2i(location, b2i(x), b2i(y))
            case (x : Boolean, y : Boolean, z : Boolean) => gl.uniform3i(location, b2i(x), b2i(y), b2i(z))
            case (x : Boolean, y : Boolean, z : Boolean, w : Boolean) => gl.uniform4i(location, b2i(x), b2i(y), b2i(z), b2i(w))

            case other => throw new RuntimeException(s"Unsupported uniform: $other")
        }
    }

    def setUniforms(): Unit = uniforms.foreach(setUniform)

    def draw() {
        setUniforms()
        gl.drawArrays(WebGLRenderingContext.TRIANGLES, 0, 6)
    }

    def fixViewport() {
        gl.viewport(0, 0, canvas.width, canvas.height)
    }

    def resize(width : Int, height : Int) {
        if(canvas.width == width && canvas.height == height) return
        canvas.width = width
        canvas.height = height
        fixViewport()
        draw()
    }

    setupContext()
    fixViewport()
}
*/