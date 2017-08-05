package dk.mzw.scalasprites

import dk.mzw.scalasprites.ScalaSprites.{Image, Scene, Sprite}

object Guts{

    case class Position(
        x : Double,
        y : Double
    )

    case class GameState(
        player : Position,
        shots : List[Position]
    )

    def view(load : String => Image) : GameState => Scene = {
        val playerImage = load("assets/piskel.png")
        val fireballImage = load("assets/fireball.png")

        {state =>
            val player = Sprite(
                state.player.x,
                state.player.y,
                image = playerImage
            )

            val shots = state.shots.map { shot =>
                Sprite(
                    shot.x,
                    shot.y,
                    image = fireballImage
                )
            }
            Scene(
                sprites = player :: shots
            )
        }
    }

    val initialState = GameState(
        player = Position(0, 0),
        shots = List()
    )

    def nextState(last : GameState, t : Double, dt : Double) : GameState = {
        val r = 1
        val speed = 1
        val playerPosition = Position(
            x = Math.cos(t * speed) * r,
            y = Math.sin(t * speed) * r
        )
        GameState(
            player = playerPosition,
            shots = List(
                Position(playerPosition.y, playerPosition.x)
            )
        )
    }
}