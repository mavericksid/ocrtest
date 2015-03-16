package com.ocrtest.util

import java.awt.image.BufferedImage
import com.recognition.software.jdeskew.ImageDeskew
import org.im4java.core.IMOperation

/**
 * OCR Engine and Image Pre Processing Utility
 */
object ImageUtility {

  /**
   * Deskews the image
   *
   * @image image image to be deskewed
   */
  def getSkewAngle(bufferedImage: BufferedImage) =
    (new ImageDeskew(bufferedImage).getSkewAngle * -1)

  /**
   * ImageMagick operations to refine and then binarize the image
   *
   * @param imageName name of image to binarize
   * @param convertedName name of the output file to be saved
   *
   * @return returns the operations to be done on the image
   */
  def binarizeImageOperation(imageName: String, convertedName: String): IMOperation = {
    val grayScaleOpr = new IMOperation
    grayScaleOpr.p_clone
    grayScaleOpr.blur(0, 2)

    // create the operation, add images and operators/options
    val op = new IMOperation
    op.addImage("src/main/resources/" + imageName)
    op.contrast
    op.sharpen(1)
    op.gaussianBlur(1)
    op.unsharp(10, 4, 1, 0)
    op.colorspace("gray")
    op.addSubOperation(grayScaleOpr)
    op.p_swap
    op.compose("divide")
    op.composite
    op.linearStretch(5, 0, true)
    op.unsharp(10, 4, 1, 0)
    op.enhance.enhance.enhance.enhance.enhance
    op.addImage("src/main/resources/tmp/" + convertedName)
    op
  }

  /**
   * ImageMagick operations to deskew the image
   *
   * @param skewAngle angle at which the image is skewed
   * @param convertedName name of the output file to be saved
   *
   * @return returns the operations to deskew the image
   */
  def deskewImageOperation(skewAngle: Double, convertedName: String): IMOperation = {
    val deskewOpr = new IMOperation
    deskewOpr.addImage("src/main/resources/tmp/" + convertedName)
    deskewOpr.rotate(skewAngle)
    deskewOpr.addImage("src/main/resources/tmp/" + convertedName)
    deskewOpr
  }
}
