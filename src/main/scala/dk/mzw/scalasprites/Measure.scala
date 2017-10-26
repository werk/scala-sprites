package dk.mzw.scalasprites

import scala.collection.mutable
import scala.scalajs.js

object Measure {
    private var currentTimer = new Timer
    private var t1 = now()

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
    var time = 0d
    var maxTime = 0d
    var invocations = 0

    def subTimer(label: String): Timer = timers.getOrElseUpdate(label, new Timer)

    def apply[R](action: => R): R = {
        val t1 = Measure.now()
        val r = action
        val dt = Measure.now() - t1
        time += dt
        maxTime = Math.max(maxTime, dt)
        invocations += 1
        r
    }

    def showList(indent: String): List[String] = {
        timers.toList.flatMap { case (label, timer) =>
            f"$indent$label x ${timer.invocations}: ${timer.time / timer.invocations}%.2fms ${timer.maxTime}%.2fms" :: timer.showList("  " + indent)
        }
    }

    def show : String = showList("").mkString("\n")
}
