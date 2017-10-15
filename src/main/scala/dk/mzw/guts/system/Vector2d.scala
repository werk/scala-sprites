package dk.mzw.guts.system

case class Vector2d(var x : Double, var y : Double) {
    def set(x : Double, y : Double) : Unit = { this.x = x; this.y = y }
    def set(v : Vector2d) : Unit = { this.x = v.x; this.y = v.y }
    def setMultiplied(v : Vector2d, factor : Double) : Unit = { this.x = v.x * factor; this.y = v.y * factor }
    def setAngle(angle : Double, magnitude : Double) : Unit = { this.x = Math.cos(angle) * magnitude; this.y = Math.sin(angle) * magnitude }
    def add(dx : Double, dy : Double) : Unit = { x += dx; y += dy }
    def add(v : Vector2d) : Unit = { x += v.x; y += v.y }
    def sub(v : Vector2d) : Unit = { x -= v.x; y -= v.y }
    def addMultiplied(v : Vector2d, factor : Double) : Unit = { x += v.x * factor; y += v.y * factor }
    def multiply(a : Double) : Unit = { x *= a; y *= a }
    def unit() : Unit = if(x != 0 || y != 0) multiply(1 / magnitude)
    def delay(vector : Vector2d, angleStep : Double, magnitudeStep : Double, delta : Double) : Unit = {
        val a1 = angle
        val a2 = vector.angle
        val a3 = a2 - a1
        val a4 = if(Math.abs(a3) > Math.PI) -a3 else a3
        val a5 =
            if(a4 > angleStep * delta) a1 + angleStep * delta
            else if(a4 < -angleStep * delta) a1 - angleStep * delta
            else a2
        val m1 = magnitude
        val m2 = vector.magnitude
        val m3 = m2 - m1
        val m4 =
            if(m3 > magnitudeStep * delta) m1 + magnitudeStep * delta
            else if(m3 < -magnitudeStep * delta) m1 - magnitudeStep * delta
            else m2
        setAngle(a5, m4)
    }
    def magnitude : Double = Math.sqrt(x*x + y*y)
    def distanceTo(v : Vector2d) : Double = Math.sqrt((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y))
    def angleTo(v : Vector2d) : Double = Math.atan2(v.y - y, v.x - x)
    def angle : Double = Math.atan2(y, x)
}
