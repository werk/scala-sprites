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
        imageY : Double,
        var group : List[Piece]
    )

    case class GrabbedPiece(
        piece : Piece,
        grabX : Double,
        grabY : Double,
        initialX : Double,
        initialY : Double
    )

    def main(): Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)
        val animation = loader(Animations.ballz)
        val floor = loader("assets/floor.png")

        val size = 4
        val edge = 2.0 / size
        val halfEdge = edge / 2.0
        val halfSize = size / 2.0

        def center(x : Double) = (Math.round(x * halfSize + 0.5) - 0.5) / halfSize

        val positions = (for{
            ix <- 0 until size
            x = center(ix.toDouble / size * 2 - 1 + halfEdge)
            iy <- 0 until size
            y = center(iy.toDouble / size * 2 - 1 + halfEdge)
        } yield (x, y)).toList

        val pieces = positions.zip(/*scala.util.Random.shuffle*/(positions)).map{case ((x, y), (x2, y2)) =>
            val piece = Piece(
                x = x2,
                y = y2,
                imageX = x - halfEdge,
                imageY = y - halfEdge,
                group = List()
            )
            piece.group = List(piece)
            piece
        }

        def findPiece(x : Double, y : Double, notThis : Option[Piece] = None) : Option[Piece] = {
            pieces.find{p =>
                p.x - halfEdge < x && x < p.x + halfEdge &&
                    p.y - halfEdge < y && y < p.y + halfEdge && !notThis.contains(p)
            }
        }

        def dragStart(x : Double, y : Double) : Option[GrabbedPiece] = {
            findPiece(x, y).map{p =>
                GrabbedPiece(
                    p,
                    x - p.x,
                    y - p.y,
                    initialX = p.x,
                    initialY = p.y
                )
            }
        }

        def dragContinue(p : GrabbedPiece, x : Double, y : Double) {
            p.piece.x = x - p.grabX
            p.piece.y = y - p.grabY
        }

        def dragEnd(g : GrabbedPiece, x : Double, y : Double): Unit = {
            if(x < -1 || 1 < x || y < -1 || 1 < y) {
                g.piece.x = g.initialX
                g.piece.y = g.initialY
            } else {
                findPiece(x, y, Some(g.piece)).foreach { other =>
                    other.x = g.initialX
                    other.y = g.initialY
                }
                g.piece.x = center(x)
                g.piece.y = center(y)
            }
        }

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
                        imageWidth = edge,
                        imageHeight = edge,
                        x = piece.x,
                        y = piece.y,
                        width = edge,
                        height = edge,
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
