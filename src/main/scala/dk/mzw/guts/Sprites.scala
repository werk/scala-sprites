package dk.mzw.guts

import dk.mzw.scalasprites.SpriteCanvas.Loader

class Sprites(loader : Loader) {

    val bunny = loader("bunnymark/rabbitv3_batman.png")
    val wall = loader("assets/wall2.png", repeat = true)
    val floor = loader("assets/floor.png")
    val ground = loader("assets/ground2.png", repeat = true)
    val barrel = loader("assets/barrel.png")
    val skeleton = loader("assets/skeltop.png").split(24, 4)

    val topManAnimation = loader("assets/topman.png").split(24, 4)
    val topManShootingAnimation = loader("assets/topman-shooting.png").split(24, 4)
    val flameBrightImage = loader("assets/flame-bright.png")
    val flameRedImage = loader("assets/flame-red.png")

}
