package dk.mzw.puzzle

import dk.mzw.guts.Guts.secondsElapsed
import dk.mzw.guts.utility.MouseDrag
import dk.mzw.puzzle.Board._
import dk.mzw.scalasprites.SpriteCanvas.{Blending, Loader}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.Implicits.global

object Puzzle extends JSApp {

    def main(): Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)
        val animation = loader(Animations.ballz)
        val floor = loader("assets/floor.png")

        val size = 5
        val borderMin = -0.5
        val borderMax = size - 0.5
        val board = new Board(size)


        def dragStart(x : Double, y : Double) : Option[Piece] = {
            println(x, y)
            board.findPiece(x, y)
        }

        def dragContinue(p : Piece, dx : Double, dy : Double) {
            p.group.offsetX = dx
            p.group.offsetY = dy
        }

        def dragEnd(p : Piece, x : Double, y : Double): Unit = {
            if(x < borderMin || borderMax < x || y < borderMin || borderMax < y) {
                p.group.offsetX = 0
                p.group.offsetY = 0
            } else {
                val inTheWay = p.group.pieces.toList.flatMap{member =>
                    val offset = board.center(member.current._1 + p.group.offsetX, member.current._2 + p.group.offsetY)
                    board.pieces.get(offset)
                }

                p.group.pieces.toList.foreach{moved =>
                    val offset = board.center(moved.current._1 + p.group.offsetX, moved.current._2 + p.group.offsetY)
                    board.pieces.get(offset).foreach{blocking =>
                        val movedCurrent = moved.current
                        moved.current = blocking.current
                        blocking.current = movedCurrent
                    }
                }
            }
        }

        loader.complete.foreach { display =>
            val mouse = new MouseDrag[Piece](canvas, display.gameCoordinatesX, display.gameCoordinatesY, dragStart, dragContinue, dragEnd)

            // This crazy stuff is done to avoid creating and allocating a new anonymous function for each call to requestAnimationFrame
            var loopF : Double => Unit = null
            val imagePieceHalfSize = 1.0 / size
            val imagePieceSize = imagePieceHalfSize * 2
            val start : Double = secondsElapsed() - 0.01
            def loop(_t : Double) : Unit = {
                val t = start - secondsElapsed()
                display.add(floor, -10, -10, 1, 0) // TODO remove
                val image = animation(t)
                board.pieces.values.foreach{piece =>
                    display.add(
                        image = image,
                        imageX = (0.5 + piece.home._1) * 2 / size - 1,
                        imageY = (0.5 + piece.home._2) * 2 / size - 1,
                        imageWidth = imagePieceSize,
                        imageHeight = imagePieceSize,
                        x = piece.current._1 + piece.group.offsetX,
                        y = piece.current._2 + piece.group.offsetY,
                        width = 1,
                        height = 1,
                        angle = 0,
                        blending = Blending.additive
                    )
                }
                display.draw((0,0,0,1), size, centerX = -0.5, centerY = -0.5)
                dom.window.requestAnimationFrame(loopF)
            }
            loopF = loop
            loop(0)
        }
    }
}

class Board(size : Int) {

    var pieces : Map[(Int, Int), Piece] = {
        val positions = for {
            x <- 0 until size
            y <- 0 until size
        } yield (x, y)

        positions.zip(scala.util.Random.shuffle(positions)).map{case (home, current) =>
            current -> Piece(home, current, Group(Set(), 0, 0))
        }.toMap
    }
    reGroup()

    def center(x : Double, y : Double) = (Math.round(x).toInt, Math.round(y).toInt)
    def findPiece(x : Double, y : Double) : Option[Piece] = pieces.get(center(x, y))

    def reGroup() = {
        pieces = pieces.map{case (_, p) =>
            p.group.pieces = Set(p)
            p.current -> p
        }

        val horizontal = for {
            x <- (0 until size).toList
            y <- 0 until size - 1
        } yield {
            ((x, y), (x, y + 1))
        }

        val vertical = for {
            y <- (0 until size).toList
            x <- 0 until size - 1
        } yield {
            ((x, y), (x + 1, y))
        }

        (horizontal ++ vertical).foreach{case (p1, p2) =>
            val piece1 = pieces(p1)
            val piece2 = pieces(p2)
            if(piece1.delta == piece2.delta) {
                piece1.group.pieces += piece2
                piece2.group.pieces += piece1
            }
        }
    }
}

object Board {
    case class Piece(
        home : (Int, Int),
        var current : (Int, Int),
        var group : Group
    ) {
        def delta : (Int, Int) = (current._1 - home._1, current._2 - home._2)
    }

    case class Group(
        var pieces : Set[Piece],
        var offsetX : Double,
        var offsetY : Double
    )
}


/*

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
 */