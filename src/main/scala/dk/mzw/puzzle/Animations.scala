package dk.mzw.puzzle

import dk.mzw.accelemation.Language._
import dk.mzw.accelemation.Math
import dk.mzw.accelemation.util.Prelude._

object Animations {

    val ballz : Animation = {t => x => y =>
        val center = Vec2(Math.cos(t), Math.sin(t)) * 0.5
        val d = Math.length(Vec2(center.x - x, center.y - y))
        val phi = Math.atan(y, x)
        //val n = (simplexNoise(Vec3(t * 0.1, x, y)) - 0.5) * 0.2
        hsva(phi / (Math.pi * 2) + t * 0.01, 0.7, 1.0 - d * 0.7, 1)
    }

    val edges : R => R => R => R => Image = {top => left => bottom => right => x => y =>
        /*
        val s = 0.95
        val i = Math.max(
            Math.smoothstep(s, 1, x) * right,
            Math.smoothstep(s, 1, -x) * left,
            Math.smoothstep(s, 1, y) * top,
            Math.smoothstep(s, 1, -y) * bottom
        )*/
        val e = 0.02
        val i = Math.max(
            if_(x + e > 1, 1 - right, 0 : R),
            if_(x - e < -1, 1 - left, 0 : R),
            if_(y + e > 1, 1 - top, 0 : R),
            if_(y - e < -1, 1 - bottom, 0 : R)
        )
        rgba(0.2, 0.2, 0.2, i)
    }

    val cursor : Image = {x => y =>
        val d = Math.length(Vec2(x, y))
        val i = Math.smoothstep(1, 0, d)
        rgba(i, i, i, 1)
    }

}
