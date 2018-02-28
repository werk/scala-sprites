package dk.mzw.puzzle

import dk.mzw.puzzle.Board._
import dk.mzw.scalasprites.SpriteCanvas._

object Puzzle extends GameLoop("spriteCanvas")  {

    var animation : Double => CustomShader = _
    var edges : Double => Double => Double => Double => CustomShader = _
    var cursor : CustomShader = _
    var floor : CustomShader = _

    val size = 5
    val board = new Board(size)

    var mouse : MouseDrag[Piece] = _
    val imagePieceHalfSize = 1.0 / size
    val imagePieceSize = imagePieceHalfSize * 2
    val oneOverSize = 1.0 / size

    override def load(loader: Loader): Unit = {
        animation = loader(Animations.moreCars)
        edges = loader.f4(Animations.edges)
        cursor = loader(Animations.cursor)
        floor = loader("assets/floor.png")
    }

    override def onLoad(display : Display): Unit = {
        mouse = new MouseDrag[Piece](canvas, display.gameCoordinatesX, display.gameCoordinatesY, board.findPiece, board.drag, board.move)
    }

    override def update(display : Display, t : Double, dt : Double): Unit = {
        display.add(floor, -10, -10, 1, 0) // TODO remove
        val image = animation(t)
        val sorted = board.pieces.values.toList.sortBy(p => p.group.offsetX != 0 || p.group.offsetY != 0)
        sorted.foreach{piece =>
            val x = piece.current._1 + piece.group.offsetX
            val y = piece.current._2 + piece.group.offsetY
            display.add(
                image = image,
                imageX = (0.5 + piece.home._1) * 2 / size - 1 - oneOverSize,
                imageY = (0.5 + piece.home._2) * 2 / size - 1 - oneOverSize,
                imageWidth = imagePieceSize,
                imageHeight = imagePieceSize,
                x = x,
                y = y,
                width = 1,
                height = 1,
                angle = 0,
                blending = Blending.top
            )
            piece.connectedTop.update(dt)
            piece.connectedLeft.update(dt)
            piece.connectedBottom.update(dt)
            piece.connectedRight.update(dt)
            val edge = edges(piece.connectedTop.value)(piece.connectedLeft.value)(piece.connectedBottom.value)(piece.connectedRight.value)
            display.add(
                image = edge,
                x = x,
                y = y,
                width = 1,
                height = 1,
                angle = 0,
                blending = Blending.top
            )
        }
        display.add(cursor, mouse.x, mouse.y, 0.1, 0, blending = Blending.additive)
        val center = size * 0.5 - 0.5
        val h = size * Math.max(1, display.boundingBox.height / display.boundingBox.width)
        display.draw((0,0,0,1), height = h, centerX = center, centerY = center)
    }
}

class Board(size : Int) {

    var pieces : Map[(Int, Int), Piece] = {
        val positions = for {
            x <- 0 until size
            y <- 0 until size
        } yield (x, y)

        positions.zip(scala.util.Random.shuffle(positions)).map{case (home, current) =>
            current -> Piece(home, current)
        }.toMap
    }
    reGroup()

    def center(x : Double, y : Double) = (Math.round(x).toInt, Math.round(y).toInt)

    def findPiece(x : Double, y : Double) : Option[Piece] = pieces.get(center(x, y))

    def drag(p : Piece, dx : Double, dy : Double) {
        p.group.offsetX = dx
        p.group.offsetY = dy
    }

    def move(p : Piece): Unit = {
        val offsetX = Math.round(p.group.offsetX).toInt
        val offsetY = Math.round(p.group.offsetY).toInt
        val allInside = p.group.members.forall { moved =>
            val offset = (moved.current._1 + offsetX, moved.current._2 + offsetY)
            pieces.contains(offset)
        }
        if(allInside) {
            val directionX = Math.signum(offsetX)
            val directionY = Math.signum(offsetY)
            val sorted = p.group.members.sortBy(p => (-directionX * p.current._1, -directionY * p.current._2))
            sorted.foreach{moved =>
                val offset = (moved.current._1 + offsetX, moved.current._2 + offsetY)
                val blocking = pieces(offset)
                swap(blocking, moved)
            }
        }
        reGroup()
    }

    private def swap(p1 : Piece, p2 : Piece): Unit = {
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

    private def reGroup() = {
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
                if(p1._1 < p2._1) {
                    piece1.connectedRight.active = true
                    piece2.connectedLeft.active = true
                }
                if(p1._2 < p2._2) {
                    piece1.connectedTop.active = true
                    piece2.connectedBottom.active = true
                }
                if(piece1.group != piece2.group) {
                    val members = piece1.group.members ++ piece2.group.members
                    val group = piece1.group.copy(members = members)
                    members.foreach(_.group = group)
                }
            }
        }
    }
}

object Board {
    case class Piece(
        home : (Int, Int),
        var current : (Int, Int),
        var group : Group = Group(List(), 0, 0),
        var connectedTop : Transition = Transition(),
        var connectedLeft : Transition = Transition(),
        var connectedBottom : Transition = Transition(),
        var connectedRight : Transition = Transition()
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

case class Transition(
    var value : Double = 0,
    var active : Boolean = false
) {
    def update(dt : Double) : Unit = {
        if(active) value = Math.min(1, value + dt)
    }
}