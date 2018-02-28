package dk.mzw.puzzle

import dk.mzw.accelemation.Language._
import dk.mzw.accelemation.Math
import dk.mzw.accelemation.util.Prelude._
import dk.mzw.accelemation.util.Combinators._
import dk.mzw.accelemation.Global._

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

    def lines(t : R, x : R) : R = {
        if_(Math.mod(x + t, 100) < 1, 1 : R, 0 : R)
    }

    val rand = {n : R =>
        Math.fract(Math.sin(n) * 43758.5453123)
    }.global("rand")

    val cosOne = {x : R =>
        (Math.cos(x) + 1) * 0.5
    }.global("cosOne")

    val cars : Animation = {t => x => y =>
        val size = 10
        val speed = 0.5 * size
        val yi = Math.floor(y * size)
        val dx = rand(yi) * 1337
        val ft = (rand(yi) + 0.5) * speed
        val i = lines(t * ft, (x + dx) * size)
        hsva(rand(yi), 0, i * 0.5, 1)
    }

    val moreCars = addition (addition (rotate(Math.pi * 0.5) (cars)) (cars)) (ballz)

    val rings : Animation = fromPolarCoordinates{t => r => phi =>
        val i = if_(Math.mod(r * 50 + t + Math.sin(phi * 5), 5) < 1, 1 : R, 0 : R)
        hsva(t * 0.1 + r, r, i, 1)
    }



}
