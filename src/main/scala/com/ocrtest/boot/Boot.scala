package com.ocrtest.boot

import java.io.File
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import org.slf4j.LoggerFactory
import com.ocrtest.actors.ImagePrePocessor
import com.ocrtest.actors.OcrEngine
import com.ocrtest.actors.OcrEngineSupervisor
import com.ocrtest.actors.OcrPostProcessor
import com.ocrtest.util.FileIOUtility
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout

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
  val ocrConvertors = Await.result(((ocrEngineSupervisor ? (RoundRobinPool(availableProcessors / availableProcessors)
    .props(Props[OcrEngine]), "OcrConvertor")).mapTo[ActorRef]), actorCreationTimeout)

  // OCR post processing actor 
  val ocrPostProcessor = Await.result(((ocrEngineSupervisor ? (RoundRobinPool(availableProcessors / 2)
    .props(Props[OcrPostProcessor]), "OcrPostProcessor")).mapTo[ActorRef]), actorCreationTimeout)

  // images to be converted
  private val fileNames = for (
    fileName <- new File("src/main/resources/images/")
      .listFiles
      .filter(_.isFile)
      .map(_.getName)
      .toList
  ) yield fileName

  logger.info("Total number of images to process " + fileNames.size)

  if (FileIOUtility.directoryCleanUp)
    fileNames map (imageName => imagePreProcessors ! PreProcessImage(imageName))
  else
    throw new Exception("Cannot clean up old directories")

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

case class PreProcessImage(imageName: String)
case class ReadFromImage(name: String, extension: String, totalImages: Range)
case class PostProcessOcrOutput(imageName: String, ocrOutputs: List[String])
