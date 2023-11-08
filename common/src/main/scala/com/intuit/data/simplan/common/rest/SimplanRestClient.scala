package com.intuit.data.simplan.common.rest

import com.intuit.data.simplan.common.exceptions.SimplanHttpException
import com.intuit.data.simplan.global.json.SimplanJsonMapper
import com.intuit.data.simplan.global.utils.SimplanImplicits.ToJsonImplicits
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpGet, HttpPost, HttpPut, HttpUriRequest}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory

import java.io.InputStream
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/** @author Abraham, Thomas - tabraham1
  *         Created on 23-Mar-2022 at 6:30 PM
  */
class SimplanRestClient(defaultHeaders: Map[String, String], timeoutInSeconds: Int = 10) extends Serializable {
  private lazy val logger = LoggerFactory.getLogger(classOf[SimplanRestClient])

  def get[T](endpoint: String, headers: Map[String, String] = Map.empty, expectedReturnCode: List[Int] = List.empty)(implicit ct: ClassTag[T]): Try[(T, Int)] = request[T](new HttpGet(endpoint), endpoint, headers, expectedReturnCode)(ct)

  def postEntity[T](endpoint: String, payload: AnyRef, headers: Map[String, String] = Map.empty, expectedReturnCode: List[Int] = List.empty)(implicit ct: ClassTag[T]): Try[(T, Int)] = post[T](endpoint, payload.toJson, headers, expectedReturnCode)(ct)

  def putEntity[T](endpoint: String, payload: AnyRef, headers: Map[String, String] = Map.empty, expectedReturnCode: List[Int] = List.empty)(implicit ct: ClassTag[T]): Try[(T, Int)] = put[T](endpoint, payload.toJson, headers, expectedReturnCode)(ct)

  def post[T](endpoint: String, payload: String, headers: Map[String, String] = Map.empty, expectedReturnCode: List[Int] = List.empty)(implicit ct: ClassTag[T]): Try[(T, Int)] = {
    val httpPost = new HttpPost(endpoint)
    val entity = new StringEntity(payload)
    httpPost.setEntity(entity)
    request[T](httpPost, endpoint, headers, expectedReturnCode)(ct)
  }

  def put[T](endpoint: String, payload: String, headers: Map[String, String] = Map.empty, expectedReturnCode: List[Int] = List.empty)(implicit ct: ClassTag[T]): Try[(T, Int)] = {
    val httpPut = new HttpPut(endpoint)
    val entity = new StringEntity(payload)
    httpPut.setEntity(entity)
    request[T](httpPut, endpoint, headers, expectedReturnCode)(ct)
  }

  private def request[T](httpRequest: HttpUriRequest, endpoint: String, headers: Map[String, String] = Map.empty, expectedReturnCode: List[Int] = List.empty)(implicit ct: ClassTag[T]): Try[(T, Int)] = {
    Try {
      val client = getHttpClientWithTimeout(timeoutInSeconds)
      val resolvedHeaders = defaultHeaders ++ headers
      resolvedHeaders.foreach(header => httpRequest.setHeader(header._1, header._2))
      val response = client.execute(httpRequest)
      val code = response.getStatusLine.getStatusCode
      val responseEntity = response.getEntity
      val responseString: String = EntityUtils.toString(responseEntity)
      if (expectedReturnCode.nonEmpty) {
        if (expectedReturnCode.contains(code)) logger.debug(s"${httpRequest.getMethod} Request Success to $endpoint")
        else {
          logger.warn(s"${httpRequest.getMethod} Request returned unexpected return code for $endpoint. - Received $code - Expected ${expectedReturnCode.mkString(",")}")
        }
      }
      client.close()
      castToType[T](code, responseString)(ct)
    } match {
      case Success(code) => code
      case Failure(exception) =>
        logger.warn(s"Request timeout($endpoint)- ${exception.getMessage}")
        Failure(new SimplanHttpException(408, endpoint, exception))
    }
  }

  def downloadRepository(endpoint: String, headers: Map[String, String] = Map.empty): InputStream = {
    val httpRequest = new HttpGet(endpoint)
    val client = getHttpClientWithTimeout(timeoutInSeconds)
    val resolvedHeaders = defaultHeaders ++ headers
    resolvedHeaders.foreach(header => httpRequest.setHeader(header._1, header._2))
    val response = client.execute(httpRequest)
    val code = response.getStatusLine.getStatusCode
    if (code != 200) throw new SimplanHttpException(code, endpoint)
    val responseEntity = response.getEntity
    responseEntity.getContent
  }

  private def castToType[T](code: Int, responseString: String)(implicit ct: ClassTag[T]): Try[(T, Int)] = {
    if (classOf[String].isAssignableFrom(ct.runtimeClass)) {
      Success(responseString.asInstanceOf[T], code)
    } else
      Try(SimplanJsonMapper.fromJson(responseString)(ct), code)
  }

  private def getHttpClientWithTimeout(timeout: Int = 5): CloseableHttpClient = {
    val timeoutInMillis = timeout * 1000
    val config = RequestConfig.custom
        .setConnectTimeout(timeoutInMillis)
        .setConnectionRequestTimeout(timeoutInMillis)
        .setSocketTimeout(timeoutInMillis)
      .build
    HttpClientBuilder.create.setDefaultRequestConfig(config).build

  }
}

object SimplanRestClient {

  lazy val default: SimplanRestClient = {
    val defaultHeaders = Map("Accept" -> "application/json", "Content-type" -> "application/json")
    new SimplanRestClient(defaultHeaders, timeoutInSeconds = 10)
  }
  def apply(defaultHeaders: Map[String, String], timeoutInSeconds: Int): SimplanRestClient = new SimplanRestClient(defaultHeaders, timeoutInSeconds)
}
