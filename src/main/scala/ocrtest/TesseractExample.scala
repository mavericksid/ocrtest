package ocrtest

import java.io.File
import net.sourceforge.tess4j._
import scala.util.Try
import org.im4java.core.IMOperation
import org.im4java.core.GMOperation
import org.im4java.core.ConvertCmd

object OcrTest extends App {

  // pre-processes the image using ImageMagick
  (new ImagePrePocessor).preProcessImage

  // Reads pre-processed image using tesseract
  val result = (new OcrEngine).readTextFromImage

  println("Result of OCR is:\n\n" + result)

}

/**
 * Does image pre-processing for
 * enhancing and binarizing the image
 * using ImagMagick
 */
class ImagePrePocessor {

  def preProcessImage = {
    // create command
    val cmd = new ConvertCmd

    val grayScaleOpr = new IMOperation
    grayScaleOpr.p_clone
    grayScaleOpr.blur(0, 20)

    // create the operation
    val op = new IMOperation
    op.addImage("src/main/resources/sample.jpg")
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
    op.addImage("src/main/resources/out.jpg")

    // execute the operation
    cmd.run(op)
  }
}

/**
 * Reads text from the pre-processed image
 * using tess4j as a wrapper around tesseract
 */
class OcrEngine {

  /**
   * Converts the image to String
   */
  def readTextFromImage: Try[String] = {
    Try {
      val imageFile = new File("src/main/resources/out.jpg")
      val tesseractInsatnce = Tesseract.getInstance
      tesseractInsatnce.doOCR(imageFile)
    }
  }
}
