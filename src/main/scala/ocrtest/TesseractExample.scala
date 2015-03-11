package ocrtest

import java.io.File
import net.sourceforge.tess4j._
import scala.util.Try

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