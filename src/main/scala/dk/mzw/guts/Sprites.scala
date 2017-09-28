package dk.mzw.guts

import dk.mzw.scalasprites.SpriteCanvas.{Blending, Loader}

class Sprites(loader : Loader) {

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
    val flameBrightImage = loader("assets/flame-bright.png")
    val flameRedImage = loader("assets/flame-red.png")

}

object Sprites {
    import org.scalajs.dom.raw.{WebGLRenderingContext => GL}
    val deathBlending = Blending(GL.FUNC_ADD, GL.CONSTANT_COLOR, GL.ONE_MINUS_SRC_ALPHA, Some((0.4, 0.3, 0.3, 1)))
}