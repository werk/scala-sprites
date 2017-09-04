package dk.mzw.scalasprites

object PackRectangles {

    case class Rectangle (
        width : Int,
        height : Int
    )

    case class Position(
        x : Int,
        y : Int,
        rectangle: Rectangle
    )

    def apply[E](elements : List[E], getRectangle : E => Rectangle, maxWidth : Int) : (Rectangle, List[(E, Position)]) = {
        val rectangles = elements.map(e => e -> getRectangle(e))
        val sorted = rectangles.sortBy(-_._2.height)
        val (height, list) = rows(List(), sorted, maxWidth, 0)
        (Rectangle(maxWidth, height), list)
    }

    private def rows[E](packed : List[(E, Position)], sorted: List[(E, PackRectangles.Rectangle)], maxWidth : Int, offsetY : Int) : (Int, List[(E, Position)]) = sorted match {
        case List() => offsetY -> packed
        case (_, first) :: _ =>
            var x = 0
            val row = sorted.toStream.map({case (e, r) =>
                val result = (e, Position(x, offsetY, r)) -> x
                x += r.width
                result
            }).takeWhile(_._2 <= maxWidth).map(_._1)
            rows(packed ++ row.toList, sorted.drop(row.size), maxWidth, offsetY + first.height)
    }

}
