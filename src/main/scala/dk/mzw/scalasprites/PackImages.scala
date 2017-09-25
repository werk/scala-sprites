package dk.mzw.scalasprites

import dk.mzw.scalasprites.SpriteCanvas.Image
import org.scalajs.dom
import org.scalajs.dom.raw.{Event, HTMLCanvasElement, HTMLImageElement}

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

object PackImages {

    private val borderSize : Int = 10

    def apply(images : List[Image]) : Future[(HTMLImageElement, List[(Image, PackRectangles.Chop)])] = {
        val getRectangle = {p : (HTMLImageElement, Image) => PackRectangles.Box(p._1.width, p._1.height)}

        Future.sequence(images.map(i => loadImage(i.url))).flatMap{ loadedImages =>
            val (dimensions, packMap) = PackRectangles(loadedImages.zip(images), getRectangle, 1024, borderSize)
            val canvas = dom.document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
            canvas.width = nextPowerOfTwo(dimensions.width)
            canvas.height = nextPowerOfTwo(dimensions.height)
            val context = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
            for(((imageElement, image), p) <- packMap) {
                val w = imageElement.width
                val h = imageElement.height
                context.drawImage(imageElement, p.x, p.y, w, h) // Center
                if(image.repeat) {
                    val b = borderSize
                    //               (image, sx   , sy   , sWidth, sHeight, dx     , dy     , dWidth, dHeight)
                    context.drawImage(imageElement, 0    , h - b, w     , b      , p.x    , p.y - b, w     , b      ) // Top
                    context.drawImage(imageElement, 0    , h - b, b     , b      , p.x + w, p.y - b, b     , b      ) // Top right
                    context.drawImage(imageElement, 0    , 0    , b     , h      , p.x + w, p.y    , b     , h      ) // Right
                    context.drawImage(imageElement, 0    , 0    , b     , b      , p.x + w, p.y + h, b     , b      ) // Bottom right
                    context.drawImage(imageElement, 0    , 0    , w     , b      , p.x    , p.y + h, w     , b      ) // Bottom
                    context.drawImage(imageElement, w - b, 0    , b     , b      , p.x - b, p.y + h, b     , b      ) // Bottom left
                    context.drawImage(imageElement, w - b, 0    , b     , h      , p.x - b, p.y    , b     , h      ) // Left
                    context.drawImage(imageElement, w - b, h - b, b     , b      , p.x - b, p.y - b, b     , b      ) // Top left

                }
            }
            val dataUrl = canvas.toDataURL("image/png")
            loadImage(dataUrl).map{ image =>
                image -> packMap.map{case ((_, url), v) => url -> v}
            }
        }
    }

    def loadImage(imageUrl : String) : Future[HTMLImageElement] = {
        val image = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
        image.src = imageUrl
        loadImage(image)
    }

    def loadImage(image : HTMLImageElement) : Future[HTMLImageElement] = {
        if (image.complete) {
            Future.successful(image)
        } else {
            val p = Promise[HTMLImageElement]()
            image.onload = { (_: Event) =>
                p.success(image)
            }
            p.future
        }
    }

    def nextPowerOfTwo(i : Int) : Int = {
        val ps = List(2,4,8,16,32,64,128,256, 512, 1024, 2048).reverse // TODO
        val p = ps.find(_ <= i).get
        if(p == i) i else p * 2
    }
}
