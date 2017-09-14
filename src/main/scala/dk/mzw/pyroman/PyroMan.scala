package dk.mzw.pyroman

import dk.mzw.pyroman.PyroMan.{Flame, GameState, Player}
import dk.mzw.scalasprites.SpriteCanvas.{Display, Loader}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement
import scala.concurrent.ExecutionContext.Implicits.global

import scala.scalajs.js.JSApp
import scala.util.Random

class PyroMan(load : Loader, keys: Keys) {

    val topManAnimation = load("assets/topman.png").split(24, 4)
    val topManShootingAnimation = load("assets/topman-shooting.png").split(24, 4)
    val flameBrightImage = load("assets/flame-bright.png")
    val flameRedImage = load("assets/flame-red.png")

    /*
    val topManAnimation = load("assets/topman.png").split(24, 4)
    val topManShootingAnimation = load("assets/topman-shooting.png").split(24, 4)
    val flameBrightImage = load("assets/flame-bright.png")
    val flameRedImage = load("assets/flame-red.png")
    */

    def draw(display : Display): Unit = {
        display.add(
            image = if(state.player.shooting) topManShootingAnimation(state.player.walkingDistance)
            else topManAnimation(state.player.walkingDistance),
            x = state.player.position.x,
            y = state.player.position.y,
            size = 1,
            angle = state.player.angle - Math.PI * 0.5
        )

        state.shots.foreach { shot =>
            val age = state.t - shot.born
            display.add(
                image = flameRedImage,
                x = shot.position.x,
                y = shot.position.y,
                size = 0.2 + parabola(age, shot.lifetime),
                angle = shot.velocity.angle + age * shot.rotationSpeed
            )

            if (age < 0.9) {
                display.add(
                    image = flameBrightImage,
                    x = shot.position.x,
                    y = shot.position.y,
                    size = 0.1 + parabola(age, shot.lifetime - 0.3),
                    angle = shot.velocity.angle
                )
            }
        }

        display.draw(40)
    }

    def nextState(last : GameState, t : Double, dt : Double) : GameState = {
        val player = {
            val velocity = Vector(
                keys.factor(Keys.leftArrow, Keys.rightArrow),
                keys.factor(Keys.downArrow, Keys.upArrow)
            ).unit.multiply(last.player.speed)
            val deltaPosition = velocity.multiply(dt)
            val position = last.player.position.add(deltaPosition)
            val angle = if(velocity.length == 0) {
                last.player.angle
            } else {
                val velocityAngle = velocity.angle
                val lastAngle = last.player.angle
                val da = Math.atan2(Math.sin(velocityAngle - lastAngle), Math.cos(velocityAngle - lastAngle))
                lastAngle + da * dt * 5
            }

            val walkingDistance : Double = if(velocity.length == 0) {
                0
            } else {
                last.player.walkingDistance + deltaPosition.length
            }

            last.player.copy(
                position = position,
                velocity = velocity,
                angle = angle,
                shooting = keys(Keys.enter),
                walkingDistance = walkingDistance
            )
        }

        val shots = last.shots
            .filter{s => s.born > t - s.lifetime}
            .map{s => s.copy(position = s.position.add(s.velocity.multiply(dt)))}

        val speed = 10
        val newShots = if(keys(Keys.enter)) {
            val shotCount = Math.round(dt * 100).toInt
            val newFlames = List.fill(shotCount){
                val angle = (Random.nextDouble() - 0.5) * 0.2 + player.angle
                val velocityUnit = Vector(Math.cos(angle), Math.sin(angle))
                val velocity = velocityUnit.multiply(speed).add(player.velocity)
                val gunSide = Vector(velocityUnit.y, -velocityUnit.x).multiply(0.2)
                Flame(
                    position = player.position.add(velocityUnit.multiply(0.5)).add(gunSide),
                    velocity = velocity,
                    born = t,
                    lifetime = 0.4 + Random.nextDouble() * 1.2,
                    rotationSpeed = Random.nextDouble()
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

    private var state = GameState(
        player = Player(
            position = Vector(0, 0),
            velocity = Vector(0, 0),
            angle = 0,
            speed = 5,
            shooting = false,
            walkingDistance = 0
        ),
        shots = List(),
        t = 0
    )

    def update(t : Double, dt : Double): Unit = {
        state = nextState(state, t, dt)
    }

    private def parabola(x : Double, s : Double = 1) : Double = Math.max(0, (4 - 4 * (x/s)) * (x/s))
}

object PyroMan extends JSApp {

    def main() : Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)
        val keys = new Keys()
        val game = new PyroMan(loader, keys)

        loader.complete.foreach{display =>
            var lastTime = System.currentTimeMillis() * 0.001
            def loop(): Unit = {
                val time = System.currentTimeMillis() * 0.001
                val dt = time - lastTime
                game.update(time, dt)
                game.draw(display)

                lastTime = time
                dom.window.requestAnimationFrame{_ => loop()}
            }
            loop()
        }
    }

    case class Player (
        position : Vector,
        velocity : Vector,
        angle : Double,
        speed : Double,
        shooting : Boolean,
        walkingDistance : Double
    )

    case class Flame(
        position : Vector,
        velocity : Vector,
        born : Double,
        lifetime : Double,
        rotationSpeed : Double
    )

    case class GameState(
        player : Player,
        shots : List[Flame],
        t : Double
    )

}