package com.ocrtest.util

import scala.io.Source
import com.rockymadden.stringmetric.similarity.DiceSorensenMetric
import java.io.File
import scala.io.BufferedSource

object WordProcessor extends App {

  val wordList = Source.fromFile("src/main/resources/wordlist/wordlist.txt").getLines.toVector

  /**
   * Gets all the OCRed text files by image name
   *
   * @param imageName image name to pick all OCRed text files
   * @param filterStrengths filter strengths by which OCRed output files are renamed
   *
   * @return returns a list of all the ORCed image output
   */
  def getOCRedImages(imageName: String, filterStrengths: Range): List[BufferedSource] = {
    val strippedName = imageName.replaceAll("[^a-zA-Z0-9]", "")
    val images = filterStrengths map (filterStr =>
      Source.fromFile(s"src/main/resources/output/${strippedName}/${imageName}_${filterStr}"))
    images.toList
  }

  /**
   * Finds all the words with probability greater than the specified probability
   *
   * @param wordList line separated dictionary words
   * @param wordToCompare word to find probability of
   * @probability probability probability over which words should match
   *
   * @return list of higher probability words
   */
  def findHighProbabilityMatch(wordList: Vector[String], wordToCompare: String,
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
