package dk.mzw.guts.utility

import org.scalajs.dom.raw.{HTMLCanvasElement, MouseEvent}

class MouseDrag[T](
    canvas : HTMLCanvasElement,
    gameCoordinatesX : Double => Double,
    gameCoordinatesY : Double => Double,
    dragStart : (Double, Double) => Option[T],
    dragContinue : (T, Double, Double) => Unit,
    dragEnd : T => Unit
) {

    var x = 0d
    var y = 0d
    var left = false

    private var dragged : Option[(T, Double, Double)] = None

    private def stopDrag(): Unit = {
        dragged.foreach{case (t, _, _) =>
            dragEnd(t)
            dragged = None
        }
    }

    canvas.onmousemove = {e : MouseEvent => {
        x = gameCoordinatesX(e.pageX)
        y = gameCoordinatesY(e.pageY)
        dragged.foreach{case (t, startX, startY) =>
            dragContinue(t, x - startX, y - startY)
        }
    }}

    canvas.onmouseout = {_ : MouseEvent => {
        left = false
        stopDrag()
    }}

    canvas.onmousedown = {e : MouseEvent => {
        if(e.button == 0) {
            left = true
            stopDrag()
            dragged = dragStart(x, y).map((_, x, y))
            e.stopPropagation()
        }
    }}

    canvas.onmouseup = {e : MouseEvent => {
        if(e.button == 0) {
            left = false
            stopDrag()
            e.stopPropagation()
        }
    }}

}
