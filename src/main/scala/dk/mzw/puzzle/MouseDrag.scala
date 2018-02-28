package dk.mzw.puzzle

import org.scalajs.dom.raw.{HTMLCanvasElement, MouseEvent, Touch, TouchEvent}

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

    canvas.onmousemove = {e : MouseEvent => move(e.pageX, e.pageY)}
    canvas.onmouseout = {_ : MouseEvent => out()}

    canvas.onmousedown = {e : MouseEvent => {
        if(e.button == 0) {
            left = true
            start(e.pageX, e.pageY)
            e.stopPropagation()
        }
    }}

    canvas.onmouseup = {e : MouseEvent => {
        if(e.button == 0) {
            left = false
            stop()
            e.stopPropagation()
        }
    }}

    private def move(pageX : Double, pageY : Double) = {
        x = gameCoordinatesX(pageX)
        y = gameCoordinatesY(pageY)
        dragged.foreach{case (t, startX, startY) =>
            dragContinue(t, x - startX, y - startY)
        }
    }

    private def out() {
        left = false
        stop()
    }

    private def start(pageX : Double, pageY : Double) {
        stop()
        x = gameCoordinatesX(pageX)
        y = gameCoordinatesY(pageY)
        dragged = dragStart(x, y).map((_, x, y))
    }

    private def stop(): Unit = {
        dragged.foreach{case (t, _, _) =>
            dragEnd(t)
            dragged = None
        }
    }

    canvas.addEventListener("touchstart", handleStart, useCapture = false)
    canvas.addEventListener("touchmove", handleMove, useCapture = false)
    canvas.addEventListener("touchend", handleEnd, useCapture = false)
    canvas.addEventListener("touchcancel", handleCancel, useCapture = false)

    var ongoingTouch : Option[MutableTouch] = None

    def handleStart(event : TouchEvent): Unit = {
        event.preventDefault()
        if(ongoingTouch.isEmpty) {
            val touch = MutableTouch(event.changedTouches(0))
            println(s"handleStart. New $touch")
            ongoingTouch = Some(touch)
            start(touch.pageX, touch.pageY)
        } else {
            println("handleStart. Ignore")
        }
    }

    def handleMove(event : TouchEvent): Unit = {
        event.preventDefault()
        for(i <- 0 until event.changedTouches.length) yield {
            val changedTouch = event.changedTouches(i)
            if(ongoingTouch.exists(_.identifier == changedTouch.identifier)) {
                println(s"handleMove: ${changedTouch.pageX}, ${changedTouch.pageX}")
                move(changedTouch.pageX, changedTouch.pageY)
            } else {
                println(s"handleMove. Ignore")
            }
        }
    }

    def handleEnd(event : TouchEvent): Unit = {
        event.preventDefault()
        for(i <- 0 until event.changedTouches.length) yield {
            val changedTouch = event.changedTouches(i)
            if(ongoingTouch.exists(_.identifier == changedTouch.identifier)) {
                println(s"handleEnd.")
                stop()
                ongoingTouch = None
            } else {
                println(s"handleEnd. Ignore")
            }
        }
    }

    def handleCancel(event : TouchEvent): Unit = {
        event.preventDefault()
        for(i <- 0 until event.changedTouches.length) yield {
            val changedTouch = event.changedTouches(i)
            if(ongoingTouch.exists(_.identifier == changedTouch.identifier)) {
                println(s"handleCancel.")
                out()
                ongoingTouch = None
            } else {
                println(s"handleCancel. Ignore")
            }
        }
    }


    case class MutableTouch(
        identifier: Double,
        var pageX : Double,
        var pageY : Double
    )

    object MutableTouch {
        def apply(touch : Touch) : MutableTouch = MutableTouch(
            identifier = touch.identifier,
            pageX = touch.pageX,
            pageY = touch.pageY
        )
    }

}
