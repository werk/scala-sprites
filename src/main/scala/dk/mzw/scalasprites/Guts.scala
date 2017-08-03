package dk.mzw.scalasprites

import dk.mzw.scalasprites.ScalaSprites.{Image, Scene, Sprite, SpriteCanvas}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

object Guts{

    case class Position(
        x : Double,
        y : Double
    )

    case class GameState(
        player : Position,
        shots : List[Position]
    )

    def run(canvas : HTMLCanvasElement) {
        ScalaSprites.loadView(Guts.myView, canvas, Guts.onLoad)
    }

    private def myView(load : String => Image) : GameState => Scene = {
        val playerImage = load("assets/piskel.png")
        //val fireballImage = load("images/fireball.png")

        {state =>
            val player = Sprite(
                state.player.x,
                state.player.y,
                image = playerImage
            )

            /*val shots = state.shots.map { shot =>
                Sprite(
                    shot.x,
                    shot.y,
                    image = fireballImage
                )
            }*/
            Scene(
                sprites = player :: List() //shots
            )
        }
    }


    private def onLoad(spriteCanvas : SpriteCanvas[GameState]) : Unit = {

        def loop(elapsed : Double): Unit = {
            val t = elapsed * 0.001
            val r = Math.cos(t * 0.2)
            val speed = 1
            val state = GameState(
                player = Position(
                    x = Math.cos(t * speed) * r,
                    y = Math.sin(t * speed) * r
                ),
                shots = List()
            )
            spriteCanvas.draw(state)
            dom.window.requestAnimationFrame(loop _)
        }
        loop(0)
    }
}