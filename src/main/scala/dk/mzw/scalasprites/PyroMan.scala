package dk.mzw.scalasprites

import dk.mzw.scalasprites.ScalaSprites.{Image, Scene, Sprite}

import scala.util.Random

object PyroMan{

    case class Vector(x : Double, y : Double) {
        def add(dx : Double, dy : Double) = Vector(x = x + dx, y = y + dy)
        def add(v : Vector) = Vector(x = x + v.x, y = y + v.y)
        def multiply(a : Double) = Vector(x = x * a, y = y * a)
        def length = Math.sqrt(x*x + y*y)
        def unit = if(length > 0) multiply(1/length) else this
        def angle = Math.atan2(y, x)
    }

    case class Player (
        position : Vector,
        velocity : Vector,
        angle : Double,
        speed : Double
    )

    case class Flame(
        position : Vector,
        velocity : Vector,
        born : Double
    )

    case class GameState(
        player : Player,
        shots : List[Flame],
        t : Double
    )

    def view(load : String => Image) : GameState => Scene = {
        val playerImage = load("assets/piskel.png")
        val fireballImage = load("assets/fireball.png")

        {state =>
            val player = Sprite(
                state.player.position.x,
                state.player.position.y,
                image = playerImage,
                size = 40
            )

            val shots = state.shots.map { shot =>
                val age = state.t - shot.born
                Sprite(
                    shot.position.x,
                    shot.position.y,
                    image = fireballImage,
                    size = 10 + age * 10
                )
            }
            Scene(
                sprites = player :: shots
            )
        }
    }

    val initialState = GameState(
        player = Player(Vector(0, 0), Vector(0, 0), 0, 0.1),
        shots = List(),
        0
    )

    def nextState(last : GameState, t : Double, dt : Double) : GameState = {
        val player = {
            val velocity = Vector(
                Keys.factor(Keys.leftArrow, Keys.rightArrow),
                Keys.factor(Keys.downArrow, Keys.upArrow)
            ).unit.multiply(dt * last.player.speed)
            val position = last.player.position.add(velocity)
            val velocityAngle = velocity.angle
            val lastAngle = last.player.angle
            val angle = if(lastAngle > velocityAngle) lastAngle - (lastAngle - velocityAngle) * dt * 5
            else lastAngle + (velocityAngle - lastAngle) * dt * 5
            last.player.copy(
                position = position,
                velocity = velocity,
                angle = angle
            )
        }

        val expiry = t - 3
        val shots = last.shots
            .filter{s => s.born > expiry}
            .map{s => s.copy(position = s.position.add(s.velocity))}

        val newShots = if(Keys(Keys.enter)) {
            val shotCount = Math.round(dt * 1000).toInt
            val newFlames = List.fill(shotCount){
                val angle = (Random.nextDouble() - 0.5) * 0.2 + player.angle
                val velocityUnit = Vector(Math.cos(angle), Math.sin(angle))
                val velocity = velocityUnit.multiply(0.2 * dt).add(player.velocity)
                Flame(
                    position = player.position.add(velocityUnit.multiply(0.01)),
                    velocity = velocity,
                    born = t
                )
            }
            newFlames ++ shots
        } else shots

        GameState(
            player = player,
            shots = newShots,
            t = t
        )
    }
}