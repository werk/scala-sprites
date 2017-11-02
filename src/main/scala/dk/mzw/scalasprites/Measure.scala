package dk.mzw.scalasprites

import scala.collection.mutable
import scala.scalajs.js

object Measure {
    private var currentTimer = new Timer
    private var t1 = now()

    def frame[R](action : => R) : R = {
        val r = currentTimer(action)
        currentTimer.newFrame()
        r
    }

    def apply[R](label : String) (action : => R) : R = {
        val lastTimer = currentTimer
        currentTimer = currentTimer.subTimer(label)
        val r = currentTimer(action)
        currentTimer = lastTimer
        r
    }

    def whenResult(f : String => Unit) : Unit = {
        val dt = now() - t1
        if(dt >= 1000) {
            f(resetAndShow())
            t1 = now()
        }
    }

    private def resetAndShow() : String = {
        val s = currentTimer.show
        currentTimer = new Timer
        s
    }

    def now() = js.Dynamic.global.performance.now().asInstanceOf[Double]
}

private class Timer {
    val timers = mutable.Map[String, Timer]()
    var totalTime = 0d
    var totalFrameTime = 0d
    var maxFrameTime = 0d
    var invocations = 0

    def newFrame() : Unit = {
        totalTime += totalFrameTime
        maxFrameTime = Math.max(maxFrameTime, totalFrameTime)
        totalFrameTime = 0
        timers.values.foreach(_.newFrame())
    }

    def subTimer(label: String): Timer = timers.getOrElseUpdate(label, new Timer)

    def apply[R](action: => R): R = {
        val t1 = Measure.now()
        val r = action
        val dt = Measure.now() - t1
        totalFrameTime += dt
        maxFrameTime = Math.max(maxFrameTime, dt)
        invocations += 1
        r
    }

    private def show(indentation : String, label : String, frames : Int) : String = {
        val x = invocations / frames
        (left(indentation + label + (if(x > 1) s" * $x" else ""), 32) +
            right(ms(totalTime / invocations), 10) + right(ms(maxFrameTime), 9) ::
            timers.toList.map { case (l, timer) => timer.show("  " + indentation, l, frames) }).mkString("\n")
    }

    def show : String = show("", "Frame", invocations)

    def ms(t : Double) = f"$t%.1f ms"
    def left(s : String, width : Int) = s + " " * (width - s.length)
    def right(s : String, width : Int) = " " * (width - s.length) + s

}
