package com.ocrtest.actors

import java.io.File
import scala.collection.mutable.Map
import scala.io.Source
import org.slf4j.LoggerFactory
import com.ocrtest.boot.PostProcessOcrOutput
import com.ocrtest.util.FileIOUtility
import com.rockymadden.stringmetric.similarity.DiceSorensenMetric
import akka.actor.Actor

class OcrPostProcessor extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val wordList =
    Source.fromFile("src/main/resources/wordlist/wordlist.txt").mkString

  def receive: Receive = {
    case PostProcessOcrOutput(imageName, ocrOutputs) => getCombinedOcrOutput(imageName, ocrOutputs)
  }

  /**
   * Post process the OCR output to increase the accuracy
   *
   * @param imageName image name to post process
   * @param ocrOutputs OCR outputs of filtered iterations of same images
   *
   * @return returns the combined OCR output
   */
  private def getCombinedOcrOutput(imageName: String, ocrOutputs: List[String]): String = {
    logger.info("OCR post processing for " + imageName + " started")

    val (rows, columns) = getRowsAndColumnsCount(ocrOutputs)
    val indexedOutputs = getIndexedOCROutput(ocrOutputs)
    val combinedOutput = getPostProcessedOutput(rows, columns, indexedOutputs)

    val strippedName = imageName.replaceAll("[^a-zA-Z0-9]", "")
    val convertedFile = new File(s"src/main/resources/output/${strippedName}/${imageName}_final")
    FileIOUtility.writeToFile(convertedFile)(printWriter => printWriter.println(combinedOutput))

    logger.info("OCR post processing for " + imageName + " completed")

    combinedOutput
  }

  /**
   * Gets the post processed OCR output based on the frequency
   * of each space separated group of alphabets
   *
   * @param rows total rows in OCR output file
   * @param columns total columns in OCR output file
   *
   * @return returns the combined output of OCR output iterations
   */
  private def getPostProcessedOutput(rows: Int, columns: Int,
    indexedOcrOutput: Array[Array[Map[String, Int]]]) = {
    val builtOutput: StringBuilder = new StringBuilder("")

    for (row <- (0 to rows - 1)) {
      for (column <- (0 to columns - 1)) {
        val (word, wordCount) =
          indexedOcrOutput(row)(column) maxBy { case (_, count) => count }
        builtOutput.append(word + " ")
      }
      builtOutput.append("\n")
    }

    builtOutput.toString
  }

  /**
   * Indexes filtered iterations of OCR outputs for same image
   * and creates a template based on the maximum index count of
   * each space separated group of alphabets
   *
   * @param ocrOutputs filtered iterations of OCR outputs
   *
   * @return returns OCR outputs in indexed form
   */
  private def getIndexedOCROutput(ocrOutputs: List[String]): Array[Array[Map[String, Int]]] = {
    val (rows, columns) = getRowsAndColumnsCount(ocrOutputs)
    val indexedOcrOutput: Array[Array[Map[String, Int]]] = Array.fill(rows, columns) { Map.empty }

    ocrOutputs.par map { text =>
      val rowsColumnsData = getRowColumData(text).zipWithIndex map {
        case (word, index) => (index, word.zipWithIndex map (_.swap) toMap)
      } toMap

      for (row <- (0 to rows - 1); column <- (0 to columns - 1)) {
        val mapAtPosition = indexedOcrOutput(row)(column)
        val valueAtPosition = rowsColumnsData get (row) flatMap (_.get(column)) getOrElse ("")

        val countAtPosition = mapAtPosition.get(valueAtPosition).getOrElse(0) + 1
        mapAtPosition += (valueAtPosition -> countAtPosition)
      }
    }

    indexedOcrOutput
  }

  /**
   * Separates the OCR output into rows and columns with space
   * separated group of alphabets behaving as values of each cell
   *
   * @param text OCR output
   *
   * @return returns rows and columns separated OCR output
   */
  private def getRowColumData(text: String) =
    text split ("\n") map (line => line split ("\\s") toVector) toVector

  /**
   * Calculates the mean Rows and Columns in the
   * OCR output of filtered iterations of the same image
   *
   * @param ocrOutputs filtered iterations of OCR outputs
   *
   * @return returns the rows and column count
   */
  private def getRowsAndColumnsCount(ocrOutputs: List[String]): (Int, Int) = {
    val rowsColumns = ocrOutputs map { text =>
      val rows = text split ("\n") size
      val columns = getRowColumData(text) map (_.size) max

      (rows, columns)
    }

    val (rows, _) = (rowsColumns map { case (row, _) => row } groupBy { row => row }
      maxBy { case (_, count) => count.size })
    val (columns, _) = (rowsColumns map { case (_, column) => column } groupBy { row => row }
      maxBy { case (_, count) => count.size })

    (rows, columns)
  }

  /**
   * Gets all the OCR output text files by image name
   *
   * @param imageName image name to pick all OCRed text files
   * @param filterStrengths filter strengths by which OCRed output files are renamed
   *
   * @return returns a list of all the ORCed image output
   */
  private def getOCRedImages(imageName: String, filterStrengths: Range): List[String] = {
    val strippedName = imageName.replaceAll("[^a-zA-Z0-9]", "")
    val images = filterStrengths map { filterStr =>
      val text = Source.fromFile(s"src/main/resources/output/${strippedName}/${imageName}_${filterStr}")
      text.mkString
    }
    images.toList
  }

  /**
   * Finds all the words with probability greater than the specified probability
   *
   * @param wordList line separated dictionary words
   * @param wordToCompare word to find probability of
   * @param probability probability over which words should match
   *
   * @return list of higher probability words
   */
  private def findHighProbabilityMatch(wordList: Vector[String], wordToCompare: String,
    probability: Double) = {
    val filterHighProbabilityWords: PartialFunction[(String, Double), String] = {
      case (word: String, prob: Double) if prob >= probability => word
    }

    val foundWords = (wordList.par map { word =>
      val probablity = (DiceSorensenMetric(1).compare(word, wordToCompare).getOrElse(0D))
      (word, probablity)
    }) collect (filterHighProbabilityWords)

    foundWords
  }

}
