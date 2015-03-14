package ocrtest

import akka.actor.Actor
import org.slf4j.LoggerFactory
import org.im4java.core.ConvertCmd
import org.im4java.core.IMOperation

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

      // create command
      val cmd = new ConvertCmd

      // grayscale image operation
      val grayScaleOpr = new IMOperation
      grayScaleOpr.p_clone
      grayScaleOpr.blur(0, 20)

      // create the operation, add images and operators/options
      val op = new IMOperation
      op.addImage("src/main/resources/" + imageName)
      op.units("PixelsPerInch")
      op.density(600)
      op.contrast
      op.sharpen(1)
      op.gaussianBlur(1)
      op.unsharp(10, 4, 1, 0)
      op.colorspace("gray")
      op.addSubOperation(grayScaleOpr)
      op.compose("Divide_Src")
      op.composite
      op.unsharp(10, 4, 1, 0)
      op.enhance.enhance.enhance.enhance.enhance
      op.addImage("src/main/resources/tmp/" + convertedName)

      // executes the pre processing operations
      cmd.run(op)

      // forwards the pre processed image name to OCREngine convertor
      OcrTest.ocrConvertors forward ConvertImage(convertedName)

    } catch {
      case ex: Exception =>
        logger.error("Pre processing for image " + imageName + " failed, reason " + ex)
    }
  }
}
