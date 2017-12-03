package dk.mzw.guts

import dk.mzw.guts.entities.GutsWorldEntity._
import dk.mzw.guts.entities.GutsWorldEntity
import dk.mzw.guts.procedural.TownGenerator
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system.{Entity, Vector2d}
import dk.mzw.guts.utility.Mouse
import dk.mzw.scalasprites.Measure
import dk.mzw.scalasprites.SpriteCanvas.Loader
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLCanvasElement, HTMLElement}

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.Implicits.global

object Guts extends JSApp {
    def main(): Unit = {
        val canvas = dom.document.getElementById("spriteCanvas").asInstanceOf[HTMLCanvasElement]
        val loader = new Loader(canvas)

        val sprites = new Sprites(loader)

        val world = new GutsWorldEntity(Self("world", Entity.localClientId), sprites)

        val tileMapSize = 1
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
            if tileMap.get(x + "," + y).contains(TownGenerator.floorTile)
        } yield {
            val position = Vector2d(x * tileMapSize, y * tileMapSize)
            SpawnFloor(Self("floor-" + x + "," + y, Entity.localClientId), position)
        }

        val player = SpawnPlayer(Self("nananana", Entity.localClientId), Vector2d(0, 0))

        val barrels = for(_ <- 1 to 20) yield {
            SpawnBarrel(Self(Math.random().toString, Entity.localClientId), Vector2d(Math.random() * 100, Math.random() * 100))
        }

        val skeletons = for(_ <- 1 to 10) yield {
            var r = Math.random()
            val v = Vector2d((Math.random() - 0.5) * 100, (Math.random() - 0.5) * 100)
            r = Math.random()
            if(r < 0.25) SpawnSkeleton(Self(), v)
            else if(r < 0.50) SpawnZombie(Self(), v)
            else if(r < 0.75) SpawnScorpion(Self(), v)
            else SpawnWolf(Self(), v)
        }

        for(m <- walls ++ floors ++ barrels ++ skeletons ++ Seq(player)) {
            world.sendMessageTo(world, m)
        }

        val measureElement = dom.document.getElementById("measure").asInstanceOf[HTMLElement]
        def showMeasure(text : String): Unit = {
            measureElement.textContent = text
        }


        loader.complete.foreach { display =>
            val mouse = new Mouse(canvas, display.gameCoordinatesX, display.gameCoordinatesY)

            // This crazy stuff is done to avoid creating and allocating a new anonymous function for each call to requestAnimationFrame
            var loopF : Double => Unit = null

            var last : Double = secondsElapsed() - 0.01
            def loop(_t : Double) : Unit = {
                val now = secondsElapsed()
                val text = Measure.frame {
                    val delta = now - last
                    if (delta < 1) {
                        Measure("internalUpdate")(world.internalUpdate(display.boundingBox, mouse, delta))
                        Measure("internalDraw")(world.internalDraw(display, player.position.x, player.position.y))
                    }
                }
                Measure.whenResult(showMeasure)
                last = now
                dom.window.requestAnimationFrame(loopF)
            }
            loopF = loop
            loop(0)
        }
    }

    def secondsElapsed() : Double = {
        scalajs.js.Dynamic.global.performance.now().asInstanceOf[Double] * 0.001
    }

}
