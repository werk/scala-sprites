package dk.mzw.guts.utility

object Normal {
    def apply() : Double = {
        val u = Math.random()
        val v = Math.random()
        Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * v)
    }

}
