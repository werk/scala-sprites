package dk.mzw.guts

import dk.mzw.accelemation.Language.{Animation, Image, R, Vec2, Vec3, rgba}
import dk.mzw.accelemation.Math
import dk.mzw.accelemation.util.Prelude
import dk.mzw.accelemation.util.Prelude.gaussianOne
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Loader}

class Sprites(loader : Loader) {

    val flame : R => Image = {variance : R => x : R => y : R =>
        for {
            d <- Vec2(x, y).magnitude
            intensity <- gaussianOne(variance, d)
            i <- intensity
        } yield rgba(i, i * 0.9, i * 0.4, 1)
    }

    val laserBeam : Animation = {t => x : R => y : R =>
        for {
            s <- Math.sin(x * 45 - t * 23) * 0.2
            w <- gaussianOne(0.05, y + s) + gaussianOne(0.05, y - s)
            b <- gaussianOne(0.4, y)
        } yield rgba(0.5*w, 0.5*w + 0.2*b, 0.2*w + 0.9*b, 1)
    }

    val wall = loader("assets/wall2.png", repeat = true)
    val floor = loader("assets/floor.png")
    val ground = loader("assets/ground2.png", repeat = true)
    val barrel = loader("assets/barrel.png")
    val skeleton = loader("assets/skeltop.png").split(24, 4)
    val zombie = loader("assets/zombman.png").split(24, 4)
    val scorpion = loader("assets/scorpman.png").split(24, 4)
    val wolf = loader("assets/wolfman.png").split(24, 4)
    val turret = loader("assets/big_gun.png")

    val topManAnimation = loader("assets/topman.png").split(24, 4)
    val topManShootingAnimation = loader("assets/topman-shooting.png").split(24, 4)
    val flameBrightImage = loader("assets/flame-bright.png") //loader(flame(0.3))
    val flameRedImage = loader("assets/flame-red.png")
    val pelletImage = loader("assets/pellet.png")
    val laserBeamImage = loader(laserBeam)
    val roundFlame = loader(flame(0.2))

}

object Sprites {
    import org.scalajs.dom.raw.{WebGLRenderingContext => GL}
    val deathBlending = Blending(GL.FUNC_ADD, GL.CONSTANT_COLOR, GL.ONE_MINUS_SRC_ALPHA, Some((0.4, 0.3, 0.3, 1)))
}