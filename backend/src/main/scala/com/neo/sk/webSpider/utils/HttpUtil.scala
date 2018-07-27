package com.neo.sk.webSpider.utils

import java.io.File
import java.nio.charset.Charset

import org.asynchttpclient.request.body.multipart.FilePart
import org.asynchttpclient.{BoundRequestBuilder, DefaultAsyncHttpClient, Param, Response}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * User: Taoz
  * Date: 11/28/2016
  * Time: 17:28 PM
  *
  * update by zhangtao 2017-01-01 23:54
  * 1, remove unused import "akka.stream.Materializer"
  * 2, change "implicit val executor" to implicit in the function.
  * 3, you can import HttpUtil.Imports to use the api.
  *
  *
  *
  */

object HttpUtil {
  private val ahClientImp: DefaultAsyncHttpClient = new DefaultAsyncHttpClient()
  private val log = LoggerFactory.getLogger(this.getClass)

  object Imports extends HttpUtil

  implicit class AhcToScala[T](reqBuilder: BoundRequestBuilder) {

    def scalaExecute(): Future[Response] = {
      import org.asynchttpclient.AsyncCompletionHandler
      val result = Promise[Response]()
      reqBuilder.execute(new AsyncCompletionHandler[Response]() {
        override def onCompleted(response: Response): Response = {
          result.success(response)
          response
        }
        override def onThrowable(t: Throwable): Unit = {
          result.failure(t)
        }
      })
      result.future
    }
  }
}

trait HttpUtil {


  import HttpUtil._

  //implicit val executor: ExecutionContext

  private val ahClient: DefaultAsyncHttpClient = ahClientImp

  import collection.JavaConverters._

  private def parseResp(response: Response, charset: Charset) = {
    val body = new String(response.getResponseBodyAsBytes, charset)
    log.debug("getRequestSend response headers:" + response.getHeaders)
    log.debug("getRequestSend response body:" + body)
    if (response.getStatusCode != 200) {
      val uri = response.getUri
      val bodyLength = body.length
      val msg = s"getRequestSend http failed url = $uri, status = ${response.getStatusCode}, text = ${response.getStatusText}, body = ${body.substring(0, Math.min(bodyLength, 1024))}"
      log.warn(msg)
    }
    body
  }

  private def executeRequest(
    methodName: String,
    request: BoundRequestBuilder,
    charset: Charset
  )(implicit executor: ExecutionContext) = {
    request.scalaExecute().map { response =>
      Right(parseResp(response, charset))
    }.recover { case e: Throwable => Left(e) }
  }

  private def executeRequest(
                              methodName: String,
                              request: BoundRequestBuilder)(implicit executor: ExecutionContext) = {
    request.scalaExecute().map { response =>
      Right(response)
    }.recover { case e: Throwable => Left(e) }
  }

  def postJsonRequestSend(
    methodName: String,
    url: String,
    parameters: List[(String, String)],
    jsonStr: String,
    responseCharsetName: String = "UTF-8"
  )(implicit executor: ExecutionContext): Future[Either[Throwable, String]] = {
    log.info("Post Request [" + methodName + "] Processing...")
    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters)
    log.debug(methodName + " postData=" + jsonStr)
    val request = ahClient.
      preparePost(url).
      setFollowRedirect(true).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      addHeader("Content-Type", "application/json").
      setBody(jsonStr)
    val cs = Charset.forName(responseCharsetName)
    executeRequest(methodName, request, cs)
  }

  def postJsonRequestSendReturnResponse(
                           methodName: String,
                           url: String,
                           parameters: List[(String, String)],
                           jsonStr: String
                         )(implicit executor: ExecutionContext): Future[Either[Throwable, Response]] = {
    log.info("Post Request [" + methodName + "] Processing...")
    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters)
    log.debug(methodName + " postData=" + jsonStr)
    val request = ahClient.
      preparePost(url).
      setFollowRedirect(true).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      addHeader("Content-Type", "application/json").
      setBody(jsonStr)

    executeRequest(methodName, request)
  }

  def postWwwFormRequestSend(
                           methodName: String,
                           url: String,
                           parameters: List[(String, String)],
                           bodyForm: List[(String, String)]
                         )(implicit executor: ExecutionContext): Future[Either[Throwable, Response]] = {
    log.info("Post Request [" + methodName + "] Processing...")
    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters)
    log.debug(methodName + " postData=" + bodyForm.filter(_._1 != "password"))
    val request = ahClient.
      preparePost(url).
      setFollowRedirect(false).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      addHeader("Content-Type", "application/x-www-form-urlencoded").
      setBody(bodyForm.map{
        e =>
          e._1 + "=" + e._2
      }.mkString("&"))
    executeRequest(methodName, request)
  }

  def postWwwFormRequestSendReturnJson(
                              methodName: String,
                              url: String,
                              parameters: List[(String, String)],
                              bodyForm: List[(String, String)],
                              responseCharsetName: String = "UTF-8"
                            )(implicit executor: ExecutionContext): Future[Either[Throwable, String]] = {
    log.info("Post Request [" + methodName + "] Processing...")
    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters)
    log.debug(methodName + " postData=" + bodyForm.filter(_._1 != "password"))
    val request = ahClient.
      preparePost(url).
      setFollowRedirect(false).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      addHeader("Content-Type", "application/x-www-form-urlencoded").
      setBody(bodyForm.map{
        e =>
          e._1 + "=" + e._2
      }.mkString("&"))
    val cs = Charset.forName(responseCharsetName)
    executeRequest(methodName, request, cs)
  }

  def getRequestSend(
    methodName: String,
    url: String,
    parameters: List[(String, String)],
    responseCharsetName: String = "UTF-8"
  )(implicit executor: ExecutionContext): Future[Either[Throwable, String]] = {
    log.info("Get Request [" + methodName + "] Processing...")
    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters)
    val request = ahClient.
      prepareGet(url).
      setFollowRedirect(true).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava)
    val cs = Charset.forName(responseCharsetName)
    executeRequest(methodName, request, cs)
  }

  def postFileRequestSend(
                           methodName: String,
                           url: String,
                           parameters: List[(String, String)],
                           file: File,
                           fileName: String,
                           responseCharsetName: String = "UTF-8"
                         )(implicit executor: ExecutionContext): Future[Either[Throwable, String]] = {
    log.info("Post Request [" + methodName + "] Processing...")
    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters)
    log.debug(methodName + " postData=" + file.getName)

    val request = ahClient.
      preparePost(url).
      setFollowRedirect(true).
      setRequestTimeout(60 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      addBodyPart(new FilePart("fileUpload", file, null, null, fileName))
    val cs = Charset.forName(responseCharsetName)
    executeRequest(methodName, request, cs)
  }

}
