package com.neo.sk.webSpider.utils


import java.io.{File, FileOutputStream, IOException, InterruptedIOException}
import java.net.UnknownHostException
import java.nio.charset.CodingErrorAction

import javax.net.ssl.SSLException
import org.apache.http.client.{CookieStore, HttpRequestRetryHandler}
import org.apache.http._
import org.apache.http.client.config.{CookieSpecs, RequestConfig}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.{BasicCookieStore, BasicCredentialsProvider, CloseableHttpClient, HttpClientBuilder}
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.Future
import com.neo.sk.webSpider.Boot.executor
import com.neo.sk.webSpider.common.AppSettings
import com.neo.sk.webSpider.models.dao.{ArticleDao, IssueDao}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.config.{ConnectionConfig, MessageConstraints}
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.protocol.HttpContext

/**
  * Created by Zhong on 2017/7/20.
  */
object HttpClientUtil {

  private val log = LoggerFactory.getLogger(this.getClass)

  private val httpHeaders = List[Header](
    new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"),
    new BasicHeader("Accept-Encoding", "gzip, deflate"),
    new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6"),
    new BasicHeader("Cache-Control", "max-age=0"),
    new BasicHeader("Connection", "keep-alive"),
    //    new BasicHeader("Host", "m.byr.cn"),
    new BasicHeader("cookie", "I2KBRCK=1; SERVER=WZ6myaEXBLHn7S9zq1+h7w==; MAID=qAvRnu8i5NhmTsDkochSCw==; MACHINE_LAST_SEEN=2018-07-16T02%3A12%3A10.134-07%3A00; JSESSIONID=aaa2mK-IwVZXK8G2Ndjsw; __utma=188721212.1576406291.1531732337.1531732337.1531732337.1; __utmc=188721212; __utmz=188721212.1531732337.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); optimizelyEndUserId=oeu1531732337551r0.7882629861054888; optimizelySegments=%7B%7D; optimizelyBuckets=%7B%7D; timezone=480; _ga=GA1.2.1576406291.1531732337; _gid=GA1.2.1765636143.1531734802; WT_FPC=id=551c5fb3-b192-46d2-ade4-c7a9d0798d6a:lv=1531690292104:ss=1531685538896; __atuvc=30%7C29"),
    new BasicHeader("Upgrade-Insecure-Requests", "1"),
    new BasicHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36")
  )

  private val globalConfig = RequestConfig.custom()
    //    .setCookieSpec(CookieSpecs.BEST_MATCH)
    .setCircularRedirectsAllowed(false)
    .setRedirectsEnabled(false)
    .setConnectTimeout(10000)
    .setSocketTimeout(10000)
    //        .setConnectionRequestTimeout(10000)
    .build()

  private val retryHeader = new HttpRequestRetryHandler() {
    /*override def retryRequest(exception: IOException, executionCount: Int, context: HttpContext): Boolean = {
      if (executionCount >= 2) { // Do not retry if over max retry count
        return false
      }
      if (exception.isInstanceOf[InterruptedIOException]) { // Timeout
        return false
      }
      if (exception.isInstanceOf[UnknownHostException]) { // Unknown host
        return false
      }
      if (exception.isInstanceOf[ConnectTimeoutException]) { // Connection refused
        return false
      }
      if (exception.isInstanceOf[SSLException]) { // SSL handshake exception
        return false
      }
      val clientContext = HttpClientContext.adapt(context)
      val request = clientContext.getRequest
      val idempotent = !request.isInstanceOf[HttpEntityEnclosingRequest]
      if (idempotent) { // Retry if the request is considered idempotent
        return true
      }
      false
    }*/
    override def retryRequest(exception: IOException, executionCount: Int, context: HttpContext): Boolean = false
  }

  private val threadNum = 100

  val messageConstraints: MessageConstraints =
    MessageConstraints.
      custom.setMaxHeaderCount(200).
      setMaxLineLength(5000).build

  val connectionConfig: ConnectionConfig = ConnectionConfig.custom.
    setMalformedInputAction(CodingErrorAction.IGNORE).
    setUnmappableInputAction(CodingErrorAction.IGNORE).
    setCharset(Consts.UTF_8).setBufferSize(64 * 1024).
    setMessageConstraints(messageConstraints).build

  val cm = new PoolingHttpClientConnectionManager
  cm.setMaxTotal(threadNum + 5)
  cm.setDefaultMaxPerRoute(threadNum + 5)
  cm.setDefaultConnectionConfig(connectionConfig)

  lazy val httpClient: CloseableHttpClient =
    HttpClientBuilder.create()
      .setConnectionManager(cm)
      .setDefaultHeaders(httpHeaders.asJava)
      .setRetryHandler(retryHeader)
      .setDefaultRequestConfig(globalConfig).build()


  def fetch(url: String,
    proxyOption: Option[String],
    headersOp: Option[Array[Header]] = None,
    cookieStore: Option[CookieStore] = None
  ): Future[Either[String, String]] = {
    Future {
      try {
        val request = new HttpGet(url)

        headersOp.foreach(h => request.setHeaders(h))

        val clientContext = new HttpClientContext()
        if(proxyOption.getOrElse("") == "182.92.83.118:53128") {
          val credsProvider = new BasicCredentialsProvider()
          AppSettings.backupProxyIp.indices.foreach { i =>
            credsProvider.setCredentials(
              new AuthScope(AppSettings.backupProxyIp(i), AppSettings.backupProxyPort(i)),
              new UsernamePasswordCredentials(AppSettings.backupProxyUsername(i), AppSettings.backupProxyPassword(i))
            )
          }
          clientContext.setCredentialsProvider(credsProvider)
        } else {
          clientContext.setCredentialsProvider(null)
        }
        
        cookieStore.foreach { c =>
          //        log.debug(c.getCookies.toString)
          clientContext.setCookieStore(c)
        }

        //set proxy
        if (proxyOption.nonEmpty) {
          val proxy = proxyOption.get.split(":")

          val httpHost = new HttpHost(proxy(0), proxy(1).toInt)
          val config = RequestConfig.custom()
            .setProxy(httpHost)
            .setConnectTimeout(10000)
            .setSocketTimeout(10000)
            .setRedirectsEnabled(false)
            .setCircularRedirectsAllowed(false)
            .setCookieSpec(CookieSpecs.DEFAULT)
            .build()
          request.setConfig(config)
        }else{
          val config = RequestConfig.custom()
            .setConnectTimeout(30000)
            .setSocketTimeout(30000)
            .setRedirectsEnabled(false)
            .setCircularRedirectsAllowed(false)
            .setCookieSpec(CookieSpecs.DEFAULT)
            .build()
          request.setConfig(config)
        }

        val response = httpClient.execute(request, clientContext)
        val statusCode = response.getStatusLine.getStatusCode
        val entity = response.getEntity
        val str = EntityUtils.toString(entity, "utf-8")//EntityUtils.toString(entity, "utf-8")
        EntityUtils.consume(response.getEntity)
        response.close()

        if (statusCode == HttpStatus.SC_OK) {
          Right(str)
        } else {
          Left(str)
        }
//        Right(str)
      } catch {
        case e: Exception =>
          log.debug(s"fetch url:$url error: $e")
          //        e.printStackTrace()

          //删除代理
          //        if (proxyOption.nonEmpty) {
          //          deleteProxy(proxyOption.get)
          //        }

          Left(e.toString)
      }
    }
  }

  def fetchImg[A](
    url: String,
    proxyOption: Option[String],
    headersOp: Option[Array[Header]] = None,
    cookieStore: Option[CookieStore] = None
  ): Future[Either[String, (Array[Byte], String)]] = {
    Future {
      try {
        val request = new HttpGet(url)

        headersOp.foreach(h => request.setHeaders(h))

        val clientContext = new HttpClientContext()

        cookieStore.foreach { c =>
//                  log.debug(c.getCookies.toString)
          clientContext.setCookieStore(c)
        }

        //set proxy
        if (proxyOption.nonEmpty) {
          val proxy = proxyOption.get.split(":")

          val httpHost = new HttpHost(proxy(0), proxy(1).toInt)
          val config = RequestConfig.custom()
            .setProxy(httpHost)
            .setConnectTimeout(10000)
            .setSocketTimeout(30000)
            .setRedirectsEnabled(false)
            .setCircularRedirectsAllowed(false)
            .setCookieSpec(CookieSpecs.DEFAULT)
            .build()
          request.setConfig(config)
        }

        val response = httpClient.execute(request, clientContext)
        val statusCode = response.getStatusLine.getStatusCode
        log.debug(s"img: $url, response: ${response.getAllHeaders.toList}")
        val entity = response.getEntity
        val str = EntityUtils.toByteArray(entity) //EntityUtils.toString(entity, "utf-8")
        EntityUtils.consume(response.getEntity)
        response.close()

        if (statusCode == HttpStatus.SC_OK) {
          val contentType = response.getHeaders("Content-Type")
          val fileType = contentType(0).toString.split("/").last
          Right((str, fileType))
        } else {
          log.error(s"not ok error: url: $url str: $str")
          Left(statusCode.toString)
        }
      } catch {
        case e: Exception =>
          log.error(s"fetch url:$url error: $e")
          Left(e.toString)
      }
    }
  }


  def main(args: Array[String]): Unit = {
    println(s"start")

    def test(i:Int):Unit={
      IssueDao.getVolume(i).map{ as=>
        as.foreach{ r=>
          println(r)
          fetch(r.url, None, None, None).map {
            case Right(t) =>
              EducationClient.parseIssueList(t,r.id,r.title)
              IssueDao.updateVolume(r.id).map{r=>
                if(i<52){
                  test(i+1)
                }
              }
            case Left(e)=>
              println(e)
              if(i<52){
                test(i+1)
              }
          }
        }
      }
    }

//    test(1)

//
    val url1="https://www.tandfonline.com/doi/abs/10.1080/10611932.2005.11031713"
    val urlTest1="https://www.tandfonline.com/doi/full/10.1080/10611932.2016.1192382"
    val urlTest11="https://www.tandfonline.com/doi/abs/10.1080/10611932.2017.1413882"
    val urlTest2="https://www.tandfonline.com/doi/ref/10.1080/10611932.2005.11031713"
    val url2="https://www.tandfonline.com/toc/mced19/14/2-3?nav=tocList"
    val url22="https://www.tandfonline.com/toc/mced19/12/3?nav=tocList"
    fetch(urlTest1, Some("117.82.124.64:22100"), None, None).map {
      case Right(t) =>
        println("--start")

//       EducationClient.parseArticleRef(t)
//        println(EducationClient.parseArticleFull(t))
        val a=EducationClient.parseArticleFull(t,urlTest1)
        println(a)
      case Left(e) =>
        println(e)
    }


//    println(url1.replace("doi/abs","doi/ref"))
    println(s"end")
  }


}
