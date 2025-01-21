package com.intuit.data.simplan.core.aws

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.{ListObjectsV2Request, ListObjectsV2Result, ObjectListing}
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.intuit.data.simplan.common.config.SimplanAppContextConfiguration
import com.intuit.data.simplan.common.exceptions.SimplanConfigException
import com.intuit.data.simplan.common.files.{FileListing, FileUtils}
import com.intuit.data.simplan.core.Constants.AWS_SYSTEM_CONFIG_KEY
import com.intuit.data.simplan.core.util.FileOperationsUtils
import com.intuit.data.simplan.logging.Logging
import org.apache.commons.io.IOUtils

import java.io.File
import java.net.URI
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.{Failure, Success, Try}

/** @author Abraham, Thomas - tabraham1
  *         Created on 25-Mar-2022 at 9:57 AM
  */
class AmazonS3FileUtils(val s3Client: AmazonS3) extends FileUtils with Logging {

  override val schemes: List[String] = List("s3", "s3a", "s3n")

  override def readContent(path: String, charset: String): String = {
    val (bucket: String, uriPath: String) = splitS3Path(path)
    val response = s3Client.getObject(bucket, uriPath).getObjectContent
    val data = IOUtils.toString(response, charset)
    response.close()
    data
  }

  override def exists(path: String): Boolean = {
    val (bucket: String, uriPath: String) = splitS3Path(path)
    s3Client.doesObjectExist(bucket, uriPath)
  }

  override def list(path: String, recursive: Boolean = true, filter: FileListing => Boolean = _ => true): List[FileListing] = {
    val (bucket: String, uriPath: String) = splitS3Path(path)
    var listing: ObjectListing = s3Client.listObjects(bucket, uriPath)
    val summaries = new scala.collection.mutable.ListBuffer[FileListing]
    listing.getObjectSummaries.asScala
      .map(x => FileListing(x.getKey, x.getSize, x.getLastModified))
      .map(each => each.copy(fileName = s"s3://$bucket/${each.fileName}"))
      .filter(filter)
      .foreach(summaries.+=)
    while (listing.isTruncated) {
      listing = s3Client.listNextBatchOfObjects(listing)
      listing.getObjectSummaries.asScala
        .map(x => FileListing(x.getKey, x.getSize, x.getLastModified))
        .map(each => each.copy(fileName = s"s3://$bucket/${each.fileName}"))
        .filter(filter)
        .foreach(summaries.+=)

    }
    summaries
      .filter(!_.fileName.endsWith("/"))
      .filter(filter)
      .toList
  }

  override def writeContent(path: String, content: String): Boolean = {
    val (bucket: String, uriPath: String) = splitS3Path(path)
    Try(s3Client.putObject(bucket, uriPath, content)).isSuccess
  }

  private def splitS3Path(s3Path: String): (String, String) = {
    val uri = new URI(s3Path)
    val uriPath = Try(uri.getPath.substring(1)) match {
      case Success(value) => value
      case Failure(_)     => ""
    }
    (Option(uri.getHost).getOrElse(uri.getAuthority), uriPath)
  }

  override def copy(sourcePath: String, destinationPath: String): Boolean = {
    val (sourceBucketName, sourceKey) = splitS3Path(sourcePath)
    val (destinationBucketName, destinationKey) = splitS3Path(destinationPath)
    try {
      val objList = s3Client.listObjectsV2(sourceBucketName, sourceKey).getObjectSummaries.asScala
      for (oneObject <- objList) {
        val singleSrcObject = oneObject.getKey
        val destKey = destinationKey + singleSrcObject.substring(sourceKey.length)
        s3Client.copyObject(sourceBucketName, singleSrcObject, destinationBucketName, destKey)
      }
      true
    } catch {
      case ex: Exception =>
        logger.error("Copy Failed: " + ex.getMessage)
        false
    }
  }

  def downloadDirectoryToLocal(s3Path: String, localPath: String) = {
    Try {
      val (bucketName, filePath) = splitS3Path(s3Path)
      val transferManager = TransferManagerBuilder.standard.withS3Client(s3Client).build()
      val download = transferManager.downloadDirectory(bucketName, filePath, new File(localPath))
      while (!download.isDone) {
        logger.info(s"Downloading - ${download.getProgress.getPercentTransferred.toInt}% completed")
        Thread.sleep(1000)
      }
      logger.info(s"Download Complete")
      transferManager.shutdownNow()
      (localPath + "/" + filePath).replaceAllLiterally("//", "/")
    }
  }

  /**  Get the total number of file along with total file size.
    * @param s3Path
    * @return Total files, total size in bytes.
    */
  def getCountAndSize(s3Path: String): (Long, Long) = {
    val (bucketName, filePath) = splitS3Path(s3Path)
    val fPath = if (!filePath.endsWith("/")) filePath + "/" else filePath
    var fileList = List[String]()
    var totalSize = 0L
    val listObjectsRequest = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(fPath)
    var result: ListObjectsV2Result = s3Client.listObjectsV2(listObjectsRequest)
    while (result.isTruncated) {
      fileList = fileList ++ result.getObjectSummaries.asScala.map(_.getKey).toList
      totalSize = totalSize + result.getObjectSummaries.asScala.map(_.getSize).sum
      listObjectsRequest.setContinuationToken(result.getNextContinuationToken)
      result = s3Client.listObjectsV2(listObjectsRequest)
    }
    fileList = fileList ++ result.getObjectSummaries.asScala.map(_.getKey).toList
    totalSize = totalSize + result.getObjectSummaries.asScala.map(_.getSize).sum
    (fileList.size, totalSize)
  }

}

object AmazonS3FileUtils extends Logging {

  private val DEFAULT_SESSION_PREFIX = "SimplanSession-"

  def apply(simplanConfiguration: SimplanAppContextConfiguration): AmazonS3FileUtils = {
    val region: Regions = Regions.fromName(simplanConfiguration.application.region.getOrElse(Regions.DEFAULT_REGION.getName))
    Try(simplanConfiguration.getSystemConfigAs[AwsSystemConfig](AWS_SYSTEM_CONFIG_KEY)) match {
      case Success(awsSystemConfig) =>
        awsSystemConfig.sts match {
          case Some(sts) =>
            val roleToAssume = if (sts.arn != null) Option(sts.arn.resolve) else None
            val sessionPrefix = sts.sessionPrefix.getOrElse(DEFAULT_SESSION_PREFIX)
            apply(roleToAssume, sessionPrefix, region)
          case None => apply()
        }
      case Failure(exception) =>
        exception match {
          case _: SimplanConfigException =>
            logger.info("Simplan System configuration for 'aws' not found. Proceeding with default")
            apply(None)
          case _ => throw exception
        }
    }

  }

  def apply(arn: Option[String] = None, sessionPrefix: String = DEFAULT_SESSION_PREFIX, region: Regions = Regions.US_WEST_2): AmazonS3FileUtils = {
    val s3client: AmazonS3 =
      if (arn.isDefined) {
        val awsCredentials = AmazonSTS.assumeRole(arn.get, sessionPrefix, region)
        AmazonS3ClientBuilder.standard.withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).withRegion(region).build
      } else {
        AmazonS3ClientBuilder
          .standard()
          .withRegion(region)
          .build()
      }
    new AmazonS3FileUtils(s3client)
  }

  def create(arn: Option[String] = None, sessionPrefix: String = DEFAULT_SESSION_PREFIX, region: String = Regions.US_WEST_2.getName): AmazonS3FileUtils = {
    apply(arn, sessionPrefix, Regions.fromName(region))
  }
  def main(args: Array[String]): Unit = {
    val utils = AmazonS3FileUtils()
    val path = "s3://idl-batch-ued-processed-uw2-data-lake-prd/uip/cdc-ingest/materializedData/ued_qbf_dwh/loan/"
    val directoryPattern = "[\\d]+~[\\d]+"
    val str = FileOperationsUtils.getSortedDirectory(utils, path, Some(directoryPattern))
    println(str)
    val filter = (each: FileListing) => each.fileName.endsWith("/_SUCCESS")

    val listings = utils.list(path, filter = filter)
    listings.foreach(each => println(s"${each.size} \t ${each.lastModifiedDate} \t ${each.fileName}"))
    val listing = listings.maxBy(_.lastModifiedDate)
    println("Max : " + listing)
  }

}
