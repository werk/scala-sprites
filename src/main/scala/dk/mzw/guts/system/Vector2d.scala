package dk.mzw.guts.system

case class Vector2d(var x : Double, var y : Double) {
    def set(x : Double, y : Double) : Unit = { this.x = x; this.y = y }
    def set(v : Vector2d) : Unit = { this.x = v.x; this.y = v.y }
    def add(dx : Double, dy : Double) : Unit = { x += dx; y += dy }
    def add(v : Vector2d) : Unit = { x += v.x; y += v.y }
    def multiply(a : Double) : Unit = { x *= a; y *= a }
    def unit() : Unit = if(x != 0 || y != 0) multiply(1 / magnitude)
    def magnitude : Double = Math.sqrt(x*x + y*y)
    def angle : Double = Math.atan2(y, x)
}
