package com.ocrtest.actors

import java.io.File
import java.io.PrintWriter
import org.slf4j.LoggerFactory
import com.ocrtest.boot.ReadFromImage
import akka.actor.Actor
import net.sourceforge.tess4j.Tesseract1

/**
 * Reads text from the pre-processed image
 * using tess4j as a wrapper around tesseract
 */
class OcrEngine extends Actor {

  val logger = LoggerFactory.getLogger(this.getClass)

  def receive: Receive = {
    case ReadFromImage(name, extension, filterStrengths) => readTextFromImage(name, extension, filterStrengths)
  }

  /**
   * Gets images from resources/tmp folder and
   * reads them to extract the text using
   * tesseract
   *
   * @param binarizedImageName pre processed image name
   * @return returns the read text from the image
   */
  private def readTextFromImage(name: String, extension: String, filterStrengths: Range) = {
    try {
      logger.info("OCR Converson for pre processed image " + name + " started")

      val strippedName = name.replaceAll("[^a-zA-Z0-9]", "")
      val createDir = new File(s"src/main/resources/output/${strippedName}").mkdir

      val results = filterStrengths.par map { filterStr =>
        val imageFile = new File(s"src/main/resources/tmp/${strippedName}/${name}_${filterStr}${extension}")
        val tesseractInsatnce = new Tesseract1
        val result = tesseractInsatnce.doOCR(imageFile)
        val convertedFile = new File(s"src/main/resources/output/${strippedName}/${name}_${filterStr}")
        writeToFile(convertedFile)(printWriter => printWriter.println(result))
        result
      }

      results
    } catch {
      case ex: Exception => logger.error("OCR conversion for " + name + " failed")
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
