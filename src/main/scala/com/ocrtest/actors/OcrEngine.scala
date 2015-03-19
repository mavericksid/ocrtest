package com.ocrtest.actors

import java.io.File
import org.slf4j.LoggerFactory
import com.ocrtest.boot.OcrTest
import com.ocrtest.boot.PostProcessOcrOutput
import com.ocrtest.boot.ReadFromImage
import com.ocrtest.util.FileIOUtility
import akka.actor.Actor
import net.sourceforge.tess4j.Tesseract1

/**
 * Reads text from the pre-processed image
 * using tess4j as a wrapper around tesseract
 */
class OcrEngine extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass)

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
  private def readTextFromImage(name: String, extension: String, filterStrengths: Range): Unit = {
    try {
      logger.info("OCR Converson for pre processed image " + name + " started")

      val strippedName = name.replaceAll("[^a-zA-Z0-9]", "")
      val createDir = new File(s"src/main/resources/output/${strippedName}").mkdir

      val results = filterStrengths.par map { filterStr =>
        val imageFile = new File(s"src/main/resources/tmp/${strippedName}/${name}_${filterStr}${extension}")
        val tesseractInsatnce = new Tesseract1
        val result = tesseractInsatnce.doOCR(imageFile)
        val convertedFile = new File(s"src/main/resources/output/${strippedName}/${name}_${filterStr}")
        val filteredResult = result.replaceAll("(?m)^[\\s]*", "").trim
        FileIOUtility.writeToFile(convertedFile)(printWriter => printWriter.println(filteredResult))
        filteredResult
      }

      val normalizedOutputs = (1 to 2) map { normalizer =>
        val imageFile = new File(s"src/main/resources/images/${name}${extension}")
        val originalTessConversion = new Tesseract1
        val originalResult = originalTessConversion.doOCR(imageFile)

        val convertedFile = new File(s"src/main/resources/output/${strippedName}/${name}_original_${normalizer}")
        val filteredResult = originalResult.replaceAll("(?m)^[\\s]*", "").replaceAll("(?m)^[^a-zA-Z0-9]*", "").trim
        FileIOUtility.writeToFile(convertedFile)(printWriter => printWriter.println(filteredResult))
        filteredResult
      } toList

      // forwards the pre processed image name to OCREngine convertor
      OcrTest.ocrPostProcessor forward PostProcessOcrOutput(name, (normalizedOutputs ++ results.toList))

    } catch {
      case ex: Exception => logger.error("OCR conversion for " + name + " failed")
    }
  }
}
