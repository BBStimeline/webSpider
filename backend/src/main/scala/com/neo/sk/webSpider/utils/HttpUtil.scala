package com.neo.sk.webSpider.utils

import java.io.{File, InputStream}
import java.nio.charset.Charset
import java.util
import javax.net.ssl.SSLContext

import com.neo.sk.webSpider.utils.http.{HttpClientUtil, InputStreamUtils}
import org.apache.http.{HttpStatus, NameValuePair}
import org.apache.http.conn.ssl.SSLContexts
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import org.asynchttpclient.proxy.ProxyServer
import org.asynchttpclient.request.body.multipart.FilePart
import org.asynchttpclient.{BoundRequestBuilder, DefaultAsyncHttpClient, Param, Response}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise}
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

  import org.apache.http.conn.ssl.SSLConnectionSocketFactory
//  import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
//  import org.apache.http.impl.nio.client.HttpAsyncClients

  //  val acceptingTrustStrategy = new Nothing() {
  //    def isTrusted(certificate: Array[Nothing], authType: String) = true
  //  }
  //  var sslContext : SSLContext= _
  //  try {
  //    sslContext = SSLContexts.custom.loadTrustMaterial(null, acceptingTrustStrategy).build
  //  } catch {
  //    case e: Exception =>
  //  }
  //
  //  val client: CloseableHttpAsyncClient = HttpAsyncClients.custom.setHostnameVerifier(
  //    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER).
  //    setSSLContext(sslContext).build

  private val ahClientImp = new DefaultAsyncHttpClient()
  //  val httpclient = HttpAsyncClientBuilder.create()
  //    .setMaxConnTotal(1000).setMaxConnPerRoute(1000).build()
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
    //    log.debug("getRequestSend response headers:" + response.getHeaders)
    //    log.debug("getRequestSend response body:" + body)
    log.debug("getRequestSend response:" + response.getStatusText)
    val code=response.getStatusCode
    if (code != 200) {
      val uri = response.getUri
      val bodyLength = body.length
      val msg = s"getRequestSend http failed url = $uri, status = ${code}"
      //      , text = ${response.getStatusText}, body = ${body.substring(0, Math.min(bodyLength, 1024))}
      log.warn(msg)
    }
    (code,body)
  }

  private def executeRequest(
                              methodName: String,
                              request: BoundRequestBuilder,
                              charset: Charset
                            )(implicit executor: ExecutionContext) = {
    request.scalaExecute().map { response =>
      Right(parseResp(response, charset)._2)
    }.recover { case e: Throwable => Left(e) }
  }

  private def executeProxyRequest(
                                   methodName: String,
                                   request: BoundRequestBuilder,
                                   charset: Charset
                                 )(implicit executor: ExecutionContext) = {
    request.scalaExecute().map { response =>
      Right(parseResp(response, charset))
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
    //    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters +jsonStr)
    val cs = Charset.forName(responseCharsetName)
    val request = ahClient.
      preparePost(url).
      setFollowRedirect(true).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      addHeader("Content-Type", "application/json").
      setBody(jsonStr).
      setCharset(cs)
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
    val cs = Charset.forName(responseCharsetName)
    val request = ahClient.
      prepareGet(url).
      setFollowRedirect(true).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      setCharset(cs)
    executeRequest(methodName, request, cs)
  }

  def getRequestWithProxy(
                           methodName: String,
                           url: String,
                           parameters: List[(String, String)],
                           ip:String,
                           port:Int,
                           responseCharsetName: String
                         )(implicit executor: ExecutionContext): Future[Either[Throwable, (Int,String)]] = {
    log.debug(methodName + " url=" + url)
    val cs = Charset.forName(responseCharsetName)
    val proxyBuilder = new ProxyServer.Builder(ip,port)
    val request = ahClient.
      prepareGet(url).
      setFollowRedirect(true).
      setRequestTimeout(5000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      setCharset(cs).setProxyServer(proxyBuilder)
    executeProxyRequest(methodName, request, cs)
  }

  def postRequestWithProxy(
                            methodName: String,
                            url: String,
                            ip:String,
                            port:Int,
                            cookie:String,
                            body:String,
                            responseCharsetName: String = "UTF-8"
                          )(implicit executor: ExecutionContext): Future[Either[Throwable, String]]= {
    log.debug(methodName + " url=" + url)
    val cs = Charset.forName(responseCharsetName)
    val proxyBuilder = new ProxyServer.Builder(ip,port)
    val request = ahClient.
      preparePost(url).
      setHeader("Accept", "application/json, text/javascript, */*; q=0.01")
      .setHeader("Accept-Encoding", "gzip, deflate, sdch")
      .setHeader("Accept-Language", "zh-CN,zh;q=0.8")
      .setHeader("Connection", "keep-alive")
      .setHeader("Host", "m.newsmth.net")
      .setHeader("Referer", "http://m.newsmth.net")
      .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
      .setHeader("X-Requested-With", "XMLHttpRequest")
      .setHeader("cookie", cookie)
      .setBody(body).
      setFollowRedirect(true).
      setRequestTimeout(3000).
      setCharset(cs).setProxyServer(proxyBuilder)
    executeRequest(methodName, request, cs)
  }





/*  def getRequest(
                  url: String,
                  responseCharsetName: String = "utf-8"
                )  = {

    var userClient: CloseableHttpClient = null
    userClient = HttpClientUtil.getHttpClient(null, 1)
    val response = HttpClientUtil.executeHttpGet(url, userClient, null)
    val httpContent = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity), responseCharsetName)
    val httpCode = response.getStatusLine.getStatusCode
    EntityUtils.consume(response.getEntity)
    response.close()
    if (httpCode == HttpStatus.SC_OK && httpContent != null && httpContent.trim.length > 0){
      Right(httpContent)
    }
    else{
      Left(httpCode)
    }
  }*/

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
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      addBodyPart(new FilePart("fileUpload", file, null, null, fileName))
    val cs = Charset.forName(responseCharsetName)
    executeRequest(methodName, request, cs)
  }

  def postJsonRequestSendWithInputStream(
                                          methodName: String,
                                          url: String,
                                          parameters: List[(String, String)],
                                          jsonStr: String,
                                          responseCharsetName: String = "UTF-8"
                                        )(implicit executor: ExecutionContext): Future[Either[Throwable, InputStream]] = {
    log.info("Post Request [" + methodName + "] Processing...")
    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters)
    //    log.debug(methodName + " postData=" + jsonStr)
    val cs = Charset.forName(responseCharsetName)
    val request = ahClient.
      preparePost(url).
      setFollowRedirect(true).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }.asJava).
      addHeader("Content-Type", "application/json").
      setBody(jsonStr).
      setCharset(cs)

    request.scalaExecute().map { response =>
      Right(response.getResponseBodyAsStream)
    }.recover { case e: Throwable => Left(e)}

  }

}