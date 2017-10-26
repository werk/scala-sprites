package dk.mzw.guts.utility

import org.scalajs.dom.raw.{HTMLCanvasElement, MouseEvent}

class Mouse(canvas : HTMLCanvasElement, gameCoordinatesX : Double => Double, gameCoordinatesY : Double => Double) {

    var x = 0d
    var y = 0d
    var left = false

    canvas.onmousemove = {e : MouseEvent => {
        x = gameCoordinatesX(e.pageX)
        y = gameCoordinatesY(e.pageY)
    }}

    canvas.onmouseout = {_ : MouseEvent => {
        left = false
    }}

    canvas.onmousedown = {e : MouseEvent => {
        if(e.button == 0) {
            left = true
            e.stopPropagation()
        }
    }}

    canvas.onmouseup = {e : MouseEvent => {
        if(e.button == 0) {
            left = false
            e.stopPropagation()
        }
    }}

}
