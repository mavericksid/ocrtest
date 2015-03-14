package ocrtest

import java.io.File
import java.io.PrintWriter
import scala.util.Try
import org.slf4j.LoggerFactory
import akka.actor.Actor
import akka.actor.actorRef2Scala
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.Tesseract1

/**
 * Reads text from the pre-processed image
 * using tess4j as a wrapper around tesseract
 */
class OcrEngine extends Actor {

  val logger = LoggerFactory.getLogger(this.getClass)

  def receive: Receive = {
    case ConvertImage(imageName) => readTextFromImage(imageName)
  }

  /**
   * Gets images from resources/tmp folder and
   * reads them to extract the text using
   * tesseract
   *
   * @param binarizedImageName pre processed image name
   * @return returns the read text from the image
   */
  private def readTextFromImage(binarizedImageName: String) = {
    try {
      logger.info("OCR Converson for pre processed image " + binarizedImageName + " started")
      val (name, _) = binarizedImageName splitAt (binarizedImageName lastIndexOf ".")
      val imageFile = new File("src/main/resources/tmp/" + binarizedImageName)
      val tesseractInsatnce = new Tesseract1
      val result = tesseractInsatnce.doOCR(imageFile)
      val convertedFile = new File("src/main/resources/output/" + name)
      writeToFile(convertedFile)(printWriter => printWriter.println(result))
      result
    } catch {
      case ex: Exception => logger.error("OCR conversion for " + binarizedImageName + " failed")
    }
  }

  /**
   * Writes text read from pre processed images to new txt files
   *
   * @param file new file to be written
   * @param printWriter writer for printing in the new file
   */
  private def writeToFile(file: File)(printWriter: PrintWriter => Unit) {
    val printWriterInstance = new PrintWriter(file)
    try {
      printWriter(printWriterInstance)
    } catch {
      case ex: Exception => ex.printStackTrace
    } finally {
      printWriterInstance.close
    }
  }
}
