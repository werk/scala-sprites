package dk.mzw.scalasprites

object PackRectangles {

    case class Box (
        width : Int,
        height : Int
    )

    case class Chop(
        x : Int,
        y : Int,
        rectangle: Box
    )

    def apply[E](elements : List[E], getBox : E => Box, maxWidth : Int, emptyBorder : Int = 10) : (Box, List[(E, Chop)]) = {
        val rectangles = elements.map(e => e -> withBorder(emptyBorder, getBox(e)))
        val sorted = rectangles.sortBy(-_._2.height)
        val (height, list) = rows(List(), sorted, maxWidth, 0)
        Box(maxWidth, height) -> list.map{case (e, p) => e -> withBorder(-emptyBorder, p)}
    }

    private def withBorder(border : Int, r: Box) : Box = Box(r.width + border * 2, r.height + border * 2)
    private def withBorder(border : Int, p: Chop) : Chop = Chop(x = p.x - border, y = p.y - border, rectangle = withBorder(border, p.rectangle))

    private def rows[E](packed : List[(E, Chop)], sorted: List[(E, PackRectangles.Box)], maxWidth : Int, offsetY : Int) : (Int, List[(E, Chop)]) = sorted match {
        case List() => offsetY -> packed
        case (_, first) :: _ =>
            var x = 0
            val row = sorted.toStream.map({case (e, r) =>
                val x1 = x
                x += r.width
                val result = (e, Chop(x1, offsetY, r)) -> x
                result
            }).takeWhile(_._2 <= maxWidth).map(_._1)
            rows(packed ++ row.toList, sorted.drop(row.size), maxWidth, offsetY + first.height)
    }

}
