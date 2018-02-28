package dk.mzw.aski

import dk.mzw.puzzle.GameLoop
import dk.mzw.scalasprites.SpriteCanvas._
import dk.mzw.accelemation.Math
import dk.mzw.accelemation.Language.{R, if_}
import dk.mzw.accelemation.Language

object Aski extends GameLoop("spriteCanvas")  {

    var A : Image = _
    var / : Image = _
    var \ : Image = _
    var grid : CustomShader = _

    override def load(loader: Loader): Unit = {
        var ascii = loader("assets/chart-set-ibm-pc2.png")
        A = ascii.chop(72, 63, 8, 8)
        / = ascii.chop(40, 289, 8, 8)
        \ = ascii.chop(88, 239, 8, 8)
        val gridA : Language.Image = {x : R => y : R =>
            val i = if_(Math.mod(Math.floor(x), 2) === Math.mod(Math.floor(y), 2), 0.1 : R, 0 : R)
            Language.rgba(i, i, i, 1)
        }
        grid = loader(gridA)
    }

    override def onLoad(display: Display): Unit = {

    }

    override def update(display: Display, t: Double, dt: Double): Unit = {
        display.add(grid, 0.5, 0.5, 40, imageWidth = 40, imageHeight = 40)
        display.add(/, -1, 0)
        display.add(A, 0, 0)
        display.add(\, 1, 0)
        display.draw((0,0,0,1), 40)
    }
}
