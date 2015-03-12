package ocrtest

import java.io.File
import net.sourceforge.tess4j._
import scala.util.Try
import org.im4java.core.IMOperation
import org.im4java.core.GMOperation
import org.im4java.core.ConvertCmd

object PreProcessImage extends App {

  // create command
  val cmd = new ConvertCmd

  // create the operation
  val op = new IMOperation
  op.addImage("src/main/resources/sample.jpg")
  op.units("PixelsPerInch").density(300)
  op.colorspace("gray")
  op.p_dither
  op.colors(2)
  op.normalize
  op.addImage("src/main/resources/out.jpg")

  // execute the operation
  cmd.run(op)
}

object TesseractExample extends App {

  // File Names OCR1.jpg, OCR2.jpg, OCR3.jpg, OCR4.jpg, OCR5.jpg
  val imageFile = new File("src/main/resources/ocr1.jpg")
  val tesseractInsatnce = Tesseract.getInstance

  try {
    val result = tesseractInsatnce.doOCR(imageFile)
    println("Extracted Text from Image is:\n\n" + result)
  } catch {
    case ex: Exception => ex.printStackTrace
  }
}
