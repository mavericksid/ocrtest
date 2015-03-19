package com.ocrtest.util

import java.io.PrintWriter
import java.io.File
import org.apache.commons.io.FileUtils

object FileIOUtility {

  /**
   * Writes text read from pre processed images to new txt files
   *
   * @param file new file to be written
   * @param printWriter writer for printing in the new file
   */
  def writeToFile(file: File)(printWriter: PrintWriter => Unit) {
    val printWriterInstance = new PrintWriter(file)
    try {
      printWriter(printWriterInstance)
    } catch {
      case ex: Exception => ex.printStackTrace
    } finally {
      printWriterInstance.close
    }
  }

  /**
   * Cleans the tmp and output directories
   *
   * @return returns true if clean up is done successfully
   */
  def directoryCleanUp: Boolean = {
    FileUtils.deleteDirectory(new File("src/main/resources/tmp"))
    FileUtils.deleteDirectory(new File("src/main/resources/output"))
    val createTmp = new File(s"src/main/resources/tmp").mkdir
    val createOutput = new File(s"src/main/resources/output").mkdir
    (createTmp && createOutput)
  }

}