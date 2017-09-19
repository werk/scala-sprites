package dk.mzw.guts

import dk.mzw.guts.entities.GutsWorldEntity.{SpawnBunny, SpawnFloor, SpawnWall}
import dk.mzw.guts.entities.{BunnyEntity, FloorEntity, GutsWorldEntity, WallEntity}
import dk.mzw.guts.procedural.TownGenerator
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system.{Entity, Vector2d, WorldEntity}
import dk.mzw.scalasprites.SpriteCanvas.Loader
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.Implicits.global

object Guts extends JSApp {
    def main(): Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)

        val sprites = new Sprites(loader)

        val world = new GutsWorldEntity(Self("world", Entity.localClientId), sprites)

        val tileMapSize = 20
        val tileMapWidth = 100
        val tileMapHeight = 100
        val tileMap = TownGenerator.generate(tileMapWidth, tileMapHeight)

        val walls = for {
            x <- 0 until tileMapWidth
            y <- 0 until tileMapHeight
            if !tileMap.contains(x + "," + y)
        } yield {
            val position = Vector2d(x * tileMapSize, y * tileMapSize)
            SpawnWall(Self("wall-" + x + "," + y, Entity.localClientId), position)
        }

        val floors = for {
            x <- 0 until tileMapWidth
            y <- 0 until tileMapHeight
            if false // tileMap.get(x + "," + y).contains(TownGenerator.floorTile)
        } yield {
            val position = Vector2d(x * tileMapSize, y * tileMapSize)
            SpawnFloor(Self("floor-" + x + "," + y, Entity.localClientId), position)
        }

        val bunny = SpawnBunny(Self("nananana", Entity.localClientId), Vector2d(0, 0))

        for(m <- walls ++ floors ++ Seq(bunny)) {
            world.sendMessageTo(world, m)
        }

        loader.complete.foreach { display =>
            println("Loader complete")

            def loop(last : Double) : Unit = {
                val now = secondsElapsed()
                val delta = now - last
                if(delta < 1) {
                    world.internalUpdate(delta)
                    world.internalDraw(display, bunny.position.x, bunny.position.y)
                }
                dom.window.requestAnimationFrame{_ => loop(now)}
            }
            loop(secondsElapsed() - 0.01)
        }
    }

    def secondsElapsed() : Double = {
        scalajs.js.Dynamic.global.performance.now().asInstanceOf[Double] * 0.001
    }

}
