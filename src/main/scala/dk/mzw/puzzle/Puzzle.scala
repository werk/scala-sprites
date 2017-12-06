package dk.mzw.puzzle

import dk.mzw.guts.Guts.secondsElapsed
import dk.mzw.guts.utility.MouseDrag
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Loader}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.Implicits.global

object Puzzle extends JSApp {

    case class Piece(
        var x : Double,
        var y : Double,
        imageX : Double,
        imageY : Double
    )

    case class GrabbedPiece(
        grabX : Double,
        grabY : Double,
        piece : Piece
    )

    def main(): Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)
        val animation = loader(Animations.ballz)
        val floor = loader("assets/floor.png")
        var pieces = for{
            ix <- 0 to 9
            x = ix.toDouble / 10 * 2 - 0.9
            iy <- 0 to 9
            y = iy.toDouble / 10 * 2 - 0.9
            //if (ix % 2) == (iy % 2)
        } yield Piece(
            x = x,
            y = y,
            imageX = x,
            imageY = y
        )

        def dragStart(x : Double, y : Double) : Option[GrabbedPiece] = {
            val piece = pieces.find{p =>
                p.x - 0.1 < x && x < p.x + 0.1 &&
                p.y - 0.1 < y && y < p.y + 0.1
            }
            piece.map{p =>
                GrabbedPiece(
                    x - p.x,
                    y - p.y,
                    p
                )
            }
        }

        def dragContinue(p : GrabbedPiece, x : Double, y : Double) {
            p.piece.x = x - p.grabX
            p.piece.y = y - p.grabY
        }

        def dragEnd(p : GrabbedPiece) {}

        loader.complete.foreach { display =>
            val mouse = new MouseDrag[GrabbedPiece](canvas, display.gameCoordinatesX, display.gameCoordinatesY, dragStart, dragContinue, dragEnd)

            // This crazy stuff is done to avoid creating and allocating a new anonymous function for each call to requestAnimationFrame
            var loopF : Double => Unit = null

            val start : Double = secondsElapsed() - 0.01
            def loop(_t : Double) : Unit = {
                val t = start - secondsElapsed()
                display.add(floor, -10, -10, 1, 0) // TODO remove
                val image = animation(t)
                pieces.foreach{piece =>
                    display.add(
                        image = image,
                        imageX = piece.imageX,
                        imageY = piece.imageY,
                        imageWidth = 0.2,
                        imageHeight = 0.2,
                        x = piece.x,
                        y = piece.y,
                        width = 0.2,
                        height = 0.2,
                        angle = 0,
                        blending = Blending.additive
                    )
                }
                display.draw((0,0,0,1), 2)
                dom.window.requestAnimationFrame(loopF)
            }
            loopF = loop
            loop(0)
        }
    }
}
