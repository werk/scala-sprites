package dk.mzw.scalasprites

import org.scalajs.dom
import org.scalajs.dom.raw.{Event, HTMLCanvasElement, HTMLImageElement}

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

object PackImages {

    def apply(imageUrls : List[String]) : Future[(HTMLImageElement, Map[String, PackRectangles.Position])] = {
        val getRectangle = {p : (HTMLImageElement, String) => PackRectangles.Rectangle(p._1.width, p._1.height)}

        Future.sequence(imageUrls.map(loadImage)).flatMap{ loadedImages =>
            val (dimensions, packMap) = PackRectangles(loadedImages.zip(imageUrls), getRectangle, 1024)
            val canvas = dom.document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
            canvas.width = nextPowerOfTwo(dimensions.width)
            canvas.height = nextPowerOfTwo(dimensions.height)
            val context = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
            for(((image, _), p) <- packMap) {
                context.drawImage(image, p.x, p.y, image.width, image.height)
            }
            val dataUrl = canvas.toDataURL("image/png")
            loadImage(dataUrl).map{ image =>
                image -> packMap.map{case ((_, url), v) => url -> v}.toMap
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
