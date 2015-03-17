package com.ocrtest.actors

import java.io.File
import scala.sys.process.Process
import org.slf4j.LoggerFactory
import com.ocrtest.boot.OcrTest
import com.ocrtest.boot.PreProcessImage
import com.ocrtest.boot.ReadFromImage
import akka.actor.Actor

/**
 * Pre processes the image in order to enhance the
 * image quality and then binarize it for better
 * OCR conversion
 *
 * @author sidharth
 */
class ImagePrePocessor extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def receive: Receive = {
    case PreProcessImage(imageName) => preProcessImage(imageName)
  }

  /**
   * Gets the image by image name from resources folder
   * and pre processes it before forwarding to OCREngine
   * convertor
   *
   * @param imageName name of the image to pre process
   */
  private def preProcessImage(imageName: String): Unit = {
    try {
      logger.info("Pre processing for image " + imageName + " started")
      val (name, extension) = imageName splitAt (imageName lastIndexOf ".")
      val strippedName = name.replaceAll("[^a-zA-Z0-9]", "")

      val createDir = new File(s"src/main/resources/tmp/${strippedName}").mkdir
      val filterStrengths = (10 to 50) by 10
      val cleanTextCommands = filterStrengths map (num =>
        s"""sh ./textcleaner -g -e none -f $num -o 5 src/main/resources/images/$imageName""" +
          s""" src/main/resources/tmp/${strippedName}/${name}_${num}${extension}""")
      val processes = cleanTextCommands map (cmd => Process(cmd).run)

      // wait until all process are executed
      processes map (cmd => cmd.exitValue)

      // forwards the pre processed image name to OCREngine convertor
      OcrTest.ocrConvertors forward ReadFromImage(name, extension, filterStrengths)
    } catch {
      case ex: Exception =>
        logger.error("Pre processing for image " + imageName + " failed, reason " + ex)
    }
  }
}
