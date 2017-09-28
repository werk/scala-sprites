package dk.mzw.guts.procedural

import scala.collection.mutable

object TownGenerator {
    
    case class Room(x : Int, y : Int, width : Int, height : Int)

    val floorTile = 1
    //val wallTile = 2 // currently None
    val doorTile = 3
    val grassTile = 4

    def generate(width : Int, height : Int) : Map[String, Int] = {
        val tiles = mutable.Map[String, Int]()
        val rooms = mutable.ListBuffer[Room]()
        def occupied(x1 : Int, y1 : Int, w : Int, h : Int) : Boolean = {
            for(x <- x1 until (x1 + w)) {
                for(y <- y1 until (y1 + h)) {
                    if(x < 0 || y < 0 || x >= width || y >= height) return true
                    if(tiles.contains(x + "," + y)) return true
                }
            }
            false
        }
        def placeRooms() {
            for(_ <- 0 until 3000) {
                val x1 = (Math.random() * width * 0.5).toInt * 2 + 1
                val y1 = (Math.random() * height * 0.5).toInt * 2 + 1
                val w = (Math.pow(Math.random(), 2) * 10 + 5).toInt * 2 + 1
                val h = (Math.pow(Math.random(), 2) * 10 + 5).toInt * 2 + 1
                if(!occupied(x1, y1, w, h)) {
                    rooms += Room(x = x1, y = y1, width = w, height = h)
                    for(x <- x1 until (x1 + w)) {
                        for(y <- y1 until (y1 + h)) {
                            tiles.put(x + "," + y, floorTile)
                        }
                    }
                }
            }
        }
        def placeCorridors() {
            for(x <- 0 until width) {
                for(y <- 0 until height) {
                    if(!tiles.contains(x + "," + y)) {
                        var c = true
                        for(x2 <- (x - 1) to (x + 1)) {
                            for(y2 <- (y - 1) to (y + 1)) {
                                c = c && !tiles.get(x2 + "," + y2).contains(floorTile)
                            }
                        }
                        if(c) {
                            tiles.put(x + "," + y, grassTile)
                        }
                    }
                }
            }
        }
        def joinRooms() {
            for(x <- 0 until width) {
                for(y <- 0 until height) {
                    if(!tiles.contains(x + "," + y)) {
                        val l = tiles.get((x - 1) + "," + y).contains(floorTile)
                        val r = tiles.get((x + 1) + "," + y).contains(floorTile)
                        val u = tiles.get(x + "," + (y - 1)).contains(floorTile)
                        val d = tiles.get(x + "," + (y + 1)).contains(floorTile)
                        if((l && r) || (u && d)) {
                            tiles.put(x + "," + y, floorTile)
                        }
                    }
                }
            }
        }
        def placeDoor(x : Int, y : Int) {
            if(!tiles.contains(x + "," + y)) {
                tiles.put(x + "," + y, doorTile)
            }
        }
        def placeDoors() {
            for(room <- rooms; _ <- 1 to 4) {
                val o = Math.random()
                if(o < 0.25) placeDoor(room.x - 1, (room.y + Math.random() * room.height).toInt)
                else if(o < 0.50) placeDoor(room.x + room.width, (room.y + Math.random() * room.height).toInt)
                else if(o < 0.75) placeDoor((room.x + Math.random() * room.width).toInt, room.y - 1)
                else placeDoor((room.x + Math.random() * room.width).toInt, room.y + room.height)
            }
        }
        placeRooms()
        joinRooms()
        placeCorridors()
        placeDoors()
        tiles.toMap
    }
    
}
