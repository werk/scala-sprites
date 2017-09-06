package dk.mzw.pyroman

case class Vector(x : Double, y : Double) {
    def add(dx : Double, dy : Double) = Vector(x = x + dx, y = y + dy)
    def add(v : Vector) = Vector(x = x + v.x, y = y + v.y)
    def multiply(a : Double) = Vector(x = x * a, y = y * a)
    lazy val length = Math.sqrt(x*x + y*y)
    lazy val unit = if(length > 0) multiply(1/length) else this
    lazy val angle = Math.atan2(y, x)
}