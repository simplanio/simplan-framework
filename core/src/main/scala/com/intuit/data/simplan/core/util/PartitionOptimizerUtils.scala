package com.intuit.data.simplan.core.util

import com.intuit.data.simplan.core.aws.AmazonS3FileUtils
import com.intuit.data.simplan.logging.Logging

import java.lang.Math.round

/** @author Kiran Hiremath
  */

case class PartitionOptimizerResult(proposedPartitionCount: Option[Int], currentAverageFileSizeInMB: Option[Float], currentTotalSizeInMB: Option[Float])

object PartitionOptimizerResult {

  def apply(proposedPartitionCount: Option[Int]): PartitionOptimizerResult = {
    PartitionOptimizerResult(proposedPartitionCount, None, None)
  }
}

object PartitionOptimizerUtils extends Logging {

  def reCalculatePartitionCount(s3Path: String, optimalFileSizeInMB: Long = 120, defaultPartitionCount: Option[Int]): PartitionOptimizerResult = {
    try {
      val s3FileUtils = AmazonS3FileUtils()
      //If the location does not exist, return the default partition count
      if (!s3FileUtils.exists(s3Path)) PartitionOptimizerResult(defaultPartitionCount, None, None)

      val (totalFiles, totalSizeInBytes) = s3FileUtils.getCountAndSize(s3Path)
      if (totalFiles > 0) {
        val byteInMB = 1024 * 1024
        val optimalFileSizeInBytes = optimalFileSizeInMB * byteInMB

        val currentTotalSizeInMB = totalSizeInBytes / byteInMB.toFloat
        val currentAverageFileSizeInMB = (totalSizeInBytes / totalFiles) / byteInMB.toFloat
        logger.info(s"Total Size: $currentTotalSizeInMB MB,  Average File SIze: $currentAverageFileSizeInMB MB, Number Of Files : $totalFiles")

        val proposedPartitionCount: Option[Int] =
          if (totalSizeInBytes == 0) {
            defaultPartitionCount
          } else {
            if (round(totalSizeInBytes / optimalFileSizeInBytes.toFloat) > 0)
              Some(round(totalSizeInBytes / optimalFileSizeInBytes.toFloat))
            else Some(1)
          }
        logger.info("After Optimisation")
        logger.info(s"Total Size: $currentTotalSizeInMB MB,  Average File SIze: $currentAverageFileSizeInMB MB, Number Of Files : $totalFiles, Proposed_partitioned_count = $proposedPartitionCount")

        PartitionOptimizerResult(proposedPartitionCount, Option(currentAverageFileSizeInMB), Option(currentTotalSizeInMB))

      } else {
        logger.info("No files found in specified destination or it doesn't exists")
        logger.info("Returning default count of files")
        PartitionOptimizerResult(defaultPartitionCount, None, None)
      }
    } catch {
      case ex: Exception =>
        logger.info("Could not calc partition count")
        logger.info("Default value will be returned." + ex.getMessage)
        logger.info(s"Total Size: $None MB,  Average File SIze: $None MB, Number Of Files : $None, Proposed_partitioned_count = $defaultPartitionCount")
        PartitionOptimizerResult(defaultPartitionCount, None, None)
    }
  }

}
