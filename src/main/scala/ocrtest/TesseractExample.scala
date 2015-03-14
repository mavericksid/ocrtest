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
import akka.actor.ActorRef
import scala.concurrent.Await
import scala.util.Try

object OcrTest extends App {

  private implicit val operationTimeout = Timeout(10 seconds)
  private val actorCreationTimeout = (Timeout(1 seconds)).duration

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val availableProcessors = Runtime.getRuntime.availableProcessors

  // ActorSystem to handle OCR Conversions
  val ocrEngine = ActorSystem("OcrEngine")
  logger.info("OCR Enigne started")

  // OCR Engine Supervisor
  private val ocrEngineSupervisor = ocrEngine.actorOf(Props[OcrEngineSupervisor],
    "OcrEngineSupervisor")

  // image pre processing actor
  val imagePreProcessors = Await.result(((ocrEngineSupervisor ? (RoundRobinPool(availableProcessors / 2)
    .props(Props[ImagePrePocessor]), "ImagePreProcessor")).mapTo[ActorRef]),
    actorCreationTimeout)

  // OCR conversion actor 
  val ocrConvertors = Await.result(((ocrEngineSupervisor ? (RoundRobinPool(availableProcessors / 2)
    .props(Props[OcrEngine]), "OcrConvertor")).mapTo[ActorRef]), actorCreationTimeout)

  // images to be converted
  private val fileNames = for (
    fileName <- new File("src/main/resources/")
      .listFiles
      .filter(_.isFile)
      .map(_.getName)
      .toList
  ) yield fileName

  logger.info("Total number of images to process " + fileNames.size)

  fileNames map (imageName => imagePreProcessors ! ConvertImage(imageName))

  // ========================================================================
  // Helper methods
  // ========================================================================

  private def deleteTemporaryFiles: List[Boolean] = {
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
