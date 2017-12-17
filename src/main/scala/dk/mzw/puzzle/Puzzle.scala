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
        val cursor = loader(Animations.cursor)
        val floor = loader("assets/floor.png")

        val size = 4
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
            } else {
                val directionX = Math.signum(p.group.offsetX)
                val directionY = Math.signum(p.group.offsetY)
                val sorted = p.group.members.sortBy(p => (-directionX * p.current._1, -directionY * p.current._2))
                sorted.foreach{moved =>
                    val offset = board.center(moved.current._1 + p.group.offsetX, moved.current._2 + p.group.offsetY)
                    board.pieces.get(offset).foreach{blocking =>
                        board.swap(blocking, moved)
                    }
                }
            }
            p.group.offsetX = 0
            p.group.offsetY = 0
            board.reGroup()
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
                display.add(cursor, mouse.x, mouse.y, 0.1, 0, blending = Blending.additive)
                val center = size * 0.5 - 0.5
                display.draw((0,0,0,1), size, centerX = center, centerY = center)
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
            current -> Piece(home, current, Group(List(), 0, 0))
        }.toMap
    }
    reGroup()

    def center(x : Double, y : Double) = (Math.round(x).toInt, Math.round(y).toInt)
    def findPiece(x : Double, y : Double) : Option[Piece] = pieces.get(center(x, y))

    def swap(p1 : Piece, p2 : Piece): Unit = {
        if(p1 != p2) {
            pieces -= p1.current
            pieces -= p2.current
            val currentP1 = p1.current
            p1.current = p2.current
            p2.current = currentP1
            pieces += p1.current -> p1
            pieces += p2.current -> p2
        }
    }

    def reGroup() = {
        pieces = pieces.map{case (_, p) =>
            p.group = Group(List(p), 0, 0)
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
                val members = piece1.group.members ++ piece2.group.members
                val group = piece1.group.copy(members = members)
                members.foreach(_.group = group)
            }
        }
        println("Re-grouped:")
        pieces.foreach { case (_, p) =>
            println(s"  ${p.home} -> ${p.current}: GROUP-${p.group.id} ${p.group.members.map(_.home).mkString(", ")}")
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

    var groupId = 0
    case class Group(
        var members : List[Piece],
        var offsetX : Double,
        var offsetY : Double,
        id : Double = {groupId += 1; groupId}
    )
}
