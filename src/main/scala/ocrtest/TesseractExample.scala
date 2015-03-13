package ocrtest

import java.io.File
import scala.concurrent.duration.DurationInt
import org.slf4j.LoggerFactory
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

object OcrTest extends App {

  implicit val timeout = Timeout(20 seconds)

  val logger = LoggerFactory.getLogger(this.getClass)

  // ActorSystem to handle OCR Conversions
  val ocrEngine = ActorSystem("OcrEngine")
  logger.info("OCR Enigne started")

  // image pre processing actor
  val imagePreProcessors = ocrEngine.actorOf(RoundRobinPool(3)
    .props(Props[ImagePrePocessor]), "ImagePreProcessor")
  // OCR conversion actor 
  val ocrConvertors = ocrEngine.actorOf(RoundRobinPool(3)
    .props(Props[OcrEngine]), "OcrConvertor")

  // images to be converted
  val fileNames = for (
    fileName <- new File("src/main/resources/")
      .listFiles
      .filter(_.isFile)
      .map(_.getName)
      .toList
  ) yield fileName

  val processedImagesFutures = fileNames map (imageName =>
    (imagePreProcessors ? ConvertImage(imageName)).mapTo[String])

  val futureOfResults = Future.sequence(processedImagesFutures)

  futureOfResults onComplete {
    case Success(result) =>
      logger.info("OCR conversion completed")
      deleteTemporaryFiles
      ocrEngine.shutdown
    case Failure(ex) =>
      logger.info("Some OCR conversion failed")
      deleteTemporaryFiles
      ocrEngine.shutdown
  }

  // ========================================================================
  // Helper methods
  // ========================================================================

  def deleteTemporaryFiles: List[Boolean] = {
    val binarizedFiles = for (
      file <- new File("src/main/resources/tmp/")
        .listFiles
        .filter(_.isFile)
        .toList
    ) yield file

    binarizedFiles map (_.delete)
  }
}

case class ConvertImage(imageName: String)
