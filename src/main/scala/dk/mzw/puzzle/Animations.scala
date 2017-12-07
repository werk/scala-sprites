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
        hsva(phi / (Math.pi * 2) + t * 0.1, 0.7, 1.0 - d * 0.7, 1)
    }
}
