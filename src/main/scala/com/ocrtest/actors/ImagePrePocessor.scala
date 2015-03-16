package com.ocrtest.actors

import java.awt.image.BufferedImage
import java.io.File
import org.im4java.core.ConvertCmd
import org.slf4j.LoggerFactory
import com.ocrtest.boot.ConvertImage
import com.ocrtest.boot.OcrTest
import com.ocrtest.util.ImageUtility
import akka.actor.Actor
import javax.imageio.ImageIO

/**
 * Pre processes the image in order to enhance the
 * image quality and then binarize it for better
 * OCR conversion
 *
 * @author sidharth
 */
class ImagePrePocessor extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass)
  // create command
  private val cmd = new ConvertCmd

  def receive: Receive = {
    case ConvertImage(imageName) => preProcessImage(imageName)
  }

  /**
   * Gets the image by image name from resources folder
   * and pre processes it before forwarding to OCREngine
   * convertor
   *
   * @param imageName name of the image to pre process
   */
  private def preProcessImage(imageName: String) = {
    try {
      logger.info("Pre processing for image " + imageName + " started")
      val (name, extension) = imageName splitAt (imageName lastIndexOf ".")
      val convertedName = name + "_converted" + extension

      // binarize image
      val grayScaleImageOprs = ImageUtility.binarizeImageOperation(imageName, convertedName)
      cmd.run(grayScaleImageOprs)

      // deskew image
      val imageFile = new File("src/main/resources/tmp/" + convertedName)
      val bufferedImage: BufferedImage = ImageIO.read(imageFile)
      val skewAngle = ImageUtility.getSkewAngle(bufferedImage)
      val deskewImageOperation = ImageUtility.deskewImageOperation(skewAngle, convertedName)
      cmd.run(deskewImageOperation)

      // forwards the pre processed image name to OCREngine convertor
      OcrTest.ocrConvertors forward ConvertImage(convertedName)
    } catch {
      case ex: Exception =>
        logger.error("Pre processing for image " + imageName + " failed, reason " + ex)
    }
  }
}
