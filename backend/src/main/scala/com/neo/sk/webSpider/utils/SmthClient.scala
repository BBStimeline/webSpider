//package com.neo.sk.webSpider.utils
//
//import java.text.SimpleDateFormat
//import java.util
//import java.util.Locale
//
//import akka.http.scaladsl.model.headers.Date
//import org.slf4j.LoggerFactory
//import io.circe.parser.decode
//import io.circe.generic.auto._
//import io.circe.syntax._
//
//import scala.concurrent.Future
//import com.neo.sk.webSpider.Boot.executor
//import com.neo.sk.webSpider.core.{BoardActor, EmailActor}
//import com.neo.sk.webSpider.core.spider.TopTenActor
//import com.neo.sk.webSpider.core.BoardManager.{BoardGeneralInfo, BoardInfo}
//import com.neo.sk.webSpider.core.spider.proxy.ProxyInfo
//import com.neo.sk.webSpider.models.SlickTables
//import com.neo.sk.webSpider.models.dao.LikeDao
//import org.apache.http._
//import org.apache.http.client.CookieStore
//import org.apache.http.client.config.{CookieSpecs, RequestConfig}
//import org.apache.http.client.entity.UrlEncodedFormEntity
//import org.apache.http.client.methods.HttpPost
//import org.apache.http.client.protocol.HttpClientContext
//import org.apache.http.cookie.Cookie
//import org.apache.http.impl.client.BasicCookieStore
//import org.apache.http.message.{BasicHeader, BasicNameValuePair}
//import org.apache.http.util.EntityUtils
//import org.joda.time.DateTime
//import org.joda.time.format.DateTimeFormat
//import org.jsoup.Jsoup
//
//import scala.collection.JavaConverters._
//import com.neo.sk.webSpider.Boot.emailActor
///**
//  * Created by Zhong on 2017/8/17.
//  */
//object SmthClient extends HttpUtil with CirceSupport {
//
//  private val log = LoggerFactory.getLogger(this.getClass)
//
//
//  case class SectionListRsp(
//                             t: String,
//                             id: Option[String]
//                           )
//
//  //Accept:application/json, text/javascript, */*; q=0.01
//  //Referer:http://www.newsmth.net/nForum/
//  //User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36
//  //X-Requested-With:XMLHttpRequest
//  private val mobileHeaders = Array[Header](
//    new BasicHeader("Accept", "application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"),
//    new BasicHeader("Accept-Encoding", "gzip, deflate"),
//    new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6"),
//    //    new BasicHeader("Cache-Control", "max-age=0"),
//    new BasicHeader("Connection", "keep-alive"),
//    //    new BasicHeader("Referer", "http://www.newsmth.net/nForum/"),
////    new BasicHeader("X-Requested-With", "XMLHttpRequest"),
//    new BasicHeader("Upgrade-Insecure-Requests", "1"),
//    new BasicHeader("Host", "m.newsmth.net"),
//    new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36")
//  )
//
//  private val pcHeaders = Array[Header](
//    new BasicHeader("Accept", "application/json, text/javascript, */*; q=0.01"),
//    new BasicHeader("Accept-Encoding", "gzip, deflate"),
//    new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6"),
//    //    new BasicHeader("Cache-Control", "max-age=0"),
//    new BasicHeader("Connection", "keep-alive"),
//    //    new BasicHeader("Referer", "http://www.newsmth.net/nForum/"),
//    new BasicHeader("X-Requested-With", "XMLHttpRequest"),
//    new BasicHeader("Upgrade-Insecure-Requests", "1"),
//    new BasicHeader("Host", "www.newsmth.net"),
//    new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36")
//  )
//
//  val boardNameMap = new scala.collection.mutable.HashMap[String, String]
//
//  /*获取子分区*/
//  def getSectionList(id: String, cookieStore: CookieStore) = {
//    val url = s"http://m.newsmth.net/section/" + id
//
//    HttpClientUtil.fetch(url, None, Some(mobileHeaders), Some(cookieStore)).map {
//      case Right(str) =>
//        sectionParser(str)
//
//      case Left(e) =>
//        log.info(s"getSectionList $id error: $e")
//        (Nil, Nil)
//    }
//  }
//
//  private def sectionParser(str: String) = {
//    val doc = Jsoup.parse(str)
//    var sectionList = List[String]()
//    var boardList = List[BoardInfo]()
//    try {
//      doc.select("#m_main").get(0).select("ul").get(0).select("li").forEach { li =>
//        if (li.getElementsByTag("font").isEmpty) {
//          val a = li.select("a").get(0)
//          val href = a.attr("href")
//          val name = href.split("/").last
//          val title = a.text()
//          boardList = BoardInfo(name, title) :: boardList
//
//        } else {
//          val a = li.select("a").get(0)
//          val href = a.attr("href")
//          val name = href.split("/").last
//          //        val title = a.text()
//          //        val sec = s"sec-$name"
//          sectionList = name :: sectionList
//        }
//      }
//    } catch {
//      case e: Exception =>
//        val msg = s"sectionParser error: $e"
////        emailActor ! EmailActor.SendEmail("sectionParser Bug", msg)
//        log.info(msg)
//    }
//
//    //    log.debug(s"sectionList $sectionList")
//    (sectionList.reverse, boardList.reverse)
//  }
//
//  def getBoardGeneralInfo(sectionName: String, cookieStore: CookieStore) = {
//    val url = s"http://www.newsmth.net/nForum/#!section/$sectionName"
//
//    HttpClientUtil.fetch(url, None, Some(mobileHeaders), Some(cookieStore)).map {
//      case Right(str) =>
//        boardGeneralParser(str)
//
//      case Left(e) =>
//        log.info(s"getSectionList $sectionName error: $e")
//        Nil
//    }
//  }
//
//  def boardGeneralParser(str: String): List[BoardGeneralInfo] = {
//    log.debug(s"boardGeneralParser start!")
//    val doc = Jsoup.parse(str)
//    var boardGeneralList = List[BoardGeneralInfo]()
//    try {
//      doc.select("tbody").get(0).select("tr").forEach { tr =>
//        val title2 = tr.getElementsByClass("title_2").get(0)
//        val a = title2.select("a")
//        if (a.isEmpty) {
//          if(title2.text() == "诚征版主中") {
//            val name = tr.getElementsByClass("title_1").select("a").get(0).attr("href").split("/").last
//            val online = tr.getElementsByClass("title_4").get(0).text().toInt
//            val todayNew = tr.getElementsByClass("title_5").get(0).text().toInt
//            val moderator = ""
//            log.debug(s"get general info line :$name $online $todayNew $moderator")
//            boardGeneralList = BoardGeneralInfo(name, online, todayNew, moderator) :: boardGeneralList
//          }
//        } else {
//          val name = tr.getElementsByClass("title_1").select("a").get(0).attr("href").split("/").last
//          val online = tr.getElementsByClass("title_4").get(0).text().toInt
//          val todayNew = tr.getElementsByClass("title_5").get(0).text().toInt
//          val moderatorIterator = a.iterator()
//          val moderator = scala.collection.mutable.ListBuffer[String]()
//          while(moderatorIterator.hasNext) {
//            moderator.append(moderatorIterator.next.text())
//          }
//          log.debug(s"get general info line :$name $online $todayNew ${moderator.mkString("|")}")
//          boardGeneralList = BoardGeneralInfo(name, online, todayNew, moderator.mkString("|")) :: boardGeneralList
//        }
//      }
//    } catch {
//      case e: Exception =>
////        e.printStackTrace()
//        log.error(s"boardGeneralParser error: $e")
//    }
//    boardGeneralList
//  }
//
//  //(3, ...)您无权阅读此版面
//  //(-1, ...)其他错误导致解析失败
//  def parseBoard(content: String, url: String): Either[Int, (List[BoardActor.PostInfo], Boolean)] = {
//    val doc = Jsoup.parse(content)
//    try {
//      val uls = doc.getElementsByTag("ul")
//      val lis = uls.get(0).getElementsByTag("li")
//      var rstList = List[BoardActor.PostInfo]()
//      for (i <- 0 until lis.size) {
//        val li = lis.get(i)
//        val divs = li.getElementsByTag("div")
//        val aHref = divs.get(0).getElementsByTag("a")
//        val articleRegex = "/article/(.*)/single/(\\d+)/(.*)".r
//        val href = aHref.get(0).attr("href")
//        val clazz = aHref.get(0).attr("class")
//        val firstDot = aHref.get(0).text.charAt(0)
//
//        if (clazz != "top") {
//          href match {
//            case articleRegex(boardName, articleId, _) =>
//              rstList = if(firstDot == '●') {
//                BoardActor.PostInfo(boardName, articleId.toLong, true) :: rstList
//              } else {
//                BoardActor.PostInfo(boardName, articleId.toLong, false) :: rstList
//							}
//
//            case other =>
//              log.info(s"parseBoard match error" + other)
//          }
//        }
//      }
//      var hasNext = false
//      doc.getElementById("m_main").getElementsByTag("div").get(2).getElementsByTag("form").get(0).getElementsByTag("a").forEach {
//        a =>
//          if (a.text().contains("下页")) {
//            hasNext = true
//          }
//      }
//
//      Right((rstList, hasNext))
//    } catch {
//      case e: Exception =>
//        try {
//          val errorMsg = doc.select(".sp").select(".hl").select(".f").get(0).text()
//          if (errorMsg == "您无权阅读此版面") {
//            Left(3)
//          } else {
//            val msg = s"parseBoard $url error: $e\ncontent: $content"
////            emailActor ! EmailActor.SendEmail("parseBoard Bug", msg)
//            log.debug(msg)
//            Left(-1)
//          }
//        } catch {
//          case e: Exception =>
//            val msg = s"parseBoard $url error: $e\ncontent: $content"
////            emailActor ! EmailActor.SendEmail("parseBoard Bug", msg)
//            log.debug(msg)
//            Left(-1)
//        }
//    }
//  }
//
//  //不解析引用，只解析溯源文章id
//  //(errCode:Int, data: Option[(rPosts, nextArticleId: Option[Long])])
//  //(0, ...)成功
//  //(1, ...)文章链接错误
//  //(2, ...)驻版可读
//  //(3, ...)您无权阅读此版面
//  //(-1, ...)其他错误导致解析失败
//  def parsePost(content: String, url: String): Either[Int, (SlickTables.rPosts, Option[Long])] = {
//    try {
//      val doc = Jsoup.parse(content)
//      val boardNameDiv = doc.getElementById("wraper").getElementsByTag("div").get(2)
//      val regex = "首页.版面-(.*)\\((.*)\\)".r
//      //      log.debug(s"boardNameDiv.text(): ${boardNameDiv.text()}")
//
//      boardNameDiv.text() match {
//        case regex(boardNameCn, boardNameEn) =>
//          val navs = doc.select(".sec").select(".nav")
//
//          val nav1 = navs.get(0)
//          val aOfNav1 = nav1.select("a")
//          val topicId = aOfNav1.get(2).attr("href").split("/").last.toLong
//          val url = {
//            if (aOfNav1.get(1).text() == "展开") {
//              aOfNav1.get(1).attr("href")
//            } else {
//              aOfNav1.get(0).attr("href")
//            }
//          } //六个版面"展开"index=0
//        val articleId = url.split("=").last.toLong
//          val quoteId = {
//            var tmp = -1L
//            for (i <- 0 until aOfNav1.size()) {
//              if (aOfNav1.get(i).text() == "溯源") {
//                tmp = aOfNav1.get(i).attr("href").split("/").last.toLong
//              }
//            }
//            if (tmp == -1L) None else Some(tmp)
//          }
//
////          val nav2 = navs.get(1)
////          val aOfNav2 = nav2.select("a")
//          val nextArticle = try {
//            val id = navs.get(1).select("a").get(0).attr("href").split("/").last.toLong
//            if (id < articleId)
//              Some(id)
//            else if (id >= articleId && articleId > 1)
//              Some(articleId - 1)
//            else
//              None
//          } catch {
//            case e: Exception =>
//              if(articleId > 1) Some(articleId - 1) else None
//          }
//
//          val ul = doc.select(".list").select(".sec").get(0)
//          val title = ul.select("li").get(0).text().drop(3)
//          val mainLi = ul.select("li").get(1)
//          val author = mainLi.select(".nav").get(0).select("div").get(0).select("a")
//          val authorId = author.get(0).text()
//          val postTime = author.get(1).text()
//          val main = mainLi.select(".sp").get(0)
//          val html = main.html()
//          val text = main.text()
//
//          val images = doc.getElementsByTag("img")
//
//          var imgs = ""
//          var imgList = List[String]()
//          var imgListWithBr = List[String]()
//          images.forEach {
//            i =>
//              val src = i.attr("src")
//              val tmpArray = src.split("/")
//              if (src.startsWith("//att.newsmth.net/nForum/att/")) {
//                val img = genSmthImgUrl(tmpArray.take(8).mkString("/"))
//                imgs = "<br><img src=\'" + img + "\'>"
//                imgList = img :: imgList
//                imgListWithBr = imgs :: imgListWithBr
//              } else if (src.startsWith("/att/")) {
//                val img = "http://m.newsmth.net" + src.dropRight(7)
//                imgs = "<br><img src=\'" + img + "\'>"
//                imgList = img :: imgList
//                imgListWithBr = imgs :: imgListWithBr
//              }
//          }
//
//          val (quoteTitle, quoteAuthor, quote, parseContent) =
//            if (quoteId.nonEmpty && text.contains("的大作中提到") && text.contains("【 在")) {
//              val quoteTitle =
//                (text.split("的大作中提到").head.split("【").last.replace(" ", "") + "的大作中提到").take(80)
//              val quoteAuthor =
//                quoteTitle.drop(1).split("的大作").head.split("\\(").head.take(30)
//              val quote = {
//                val temp = text.split("大作中提到: 】")
//                val q = (if (temp.length > 2) temp(1) else temp.last).split("--").head
//                if (q.startsWith(" : ")) q.drop(3) else q
//              }.take(3000)
//
//              val content = {
//                val regex = "(\n<br>)+".r
//                val suffix = try {
//                  Some(regex.findAllIn(html.split("【").toList.drop(1).head).toList.last)
//                } catch {
//                  case e: Exception =>
//                    log.warn(s"suffix error: $e")
//                    None
//                }
//                if (html.startsWith("【 在 ")) {
//                  val arr = quote.split(" ")
//                  if (arr.nonEmpty) quote.split(" ").last else ""
//                } else if (html.startsWith("【 以下文字由")) {
//                  ("【" + html.split("【").toList.drop(1).head).stripSuffix(suffix.getOrElse(""))
//                } else {
//                  html.split("【 在 ").head.stripSuffix(suffix.getOrElse(""))
//                }}.take(24000) + imgListWithBr.mkString
//
//              (Some(quoteTitle), Some(quoteAuthor), Some(quote), content)
//            } else {
//              (None, None, None, html.split("--").headOption.getOrElse("").take(24000) + imgListWithBr.mkString)
//            }
//
//          val ipStr = text.split("--").lastOption.getOrElse("0.0.0.0").split(" ").lastOption.getOrElse("0.0.0.0").replace(" ", "")
//          val ip = if (ipStr.contains("*"))
//            ipStr else "0.0.0.0"
//
//          val timestamp = TimeUtil.date2TimeStamp(postTime)
//
//          val isMain = if (topicId == articleId) 1 else 0
//
//          Right(SlickTables.rPosts(
//            id = 0L,
//            topicId = topicId,
//            postId = articleId,
//            isMain = isMain,
//            title = title,
//            authorId = authorId,
//            contentHtml = html,
//            contentText = parseContent,
//            imgs = imgList.reverse.mkString(";"),
//            hestiaImgs = "",
//            postTime = timestamp,
//            boardName = boardNameEn,
//            url = url,
//            ip = if (ip.length >= 60) "0.0.0.0" else ip,
//            boardNameCn = boardNameCn,
//            quoteId = quoteId,
//            quoteAuthor = quoteAuthor,
//            quoteTitle = quoteTitle,
//            quoteContent = quote
//          ), nextArticle)
//
//        case other =>
//          log.error(s"parsePost $url boardNameDiv.text(): $other")
//          val errorMsg = doc.select(".sp").select(".hl").select(".f").get(0).text()
//          if (errorMsg == "指定的文章不存在或链接错误") {
//            Left(1)
//          } else if (errorMsg == "本版为驻版可读,非驻版用户不可查看或回复文章") {
//            Left(2)
//          } else if (errorMsg == "您无权阅读此版面") {
//            Left(3)
//          } else {
//            val msg = s"""parsePost $url doc.select(".sp").select(".h1").select(".f") error: $errorMsg"""
////            emailActor ! EmailActor.SendEmail("parsePost Bug", msg)
//            log.error(msg)
//            Left(-1)
//          }
//      }
//    } catch {
//      case e: Exception =>
//        val msg = s"parsePost $url error: $e\ncontent:$content"
////        emailActor ! EmailActor.SendEmail("parsePost Bug", msg)
//        log.error(msg)
//        //        e.printStackTrace()
//        //        log.error(s"parsePost error, content: $content")
//        Left(-1)
//    }
//  }
//
//  //(errCode:Int, data: List[TopTenInfo])
//  //(-1, ...)其他错误导致解析失败
//  def parseTopTen(content: String, url: String): Either[Int, List[TopTenActor.TopTenInfo]] = {
//    val doc = Jsoup.parse(content)
//    try {
//      val uls = doc.getElementsByTag("ul")
//      val lis = uls.get(1).getElementsByTag("li")
//      var rstList = List[TopTenActor.TopTenInfo]()
//
//      for (i <- 1 until lis.size) {
//        val li = lis.get(i)
//        val aHref = li.getElementsByTag("a").get(0)
//        val href = aHref.attr("href")
//        val topicWithHotRank = aHref.text()
//        val hrefRegex = "/article/(.*)/(\\d+)".r
//        val topicRegex = "(.*)[(](.*)[)]".r
//
//        href match {
//          case hrefRegex(name, num) =>
//            val boardName = name
//            val id = num.toLong
//
//            topicWithHotRank match {
//              case topicRegex(title, hotValue) =>
//                val topic = title
//                val hotRank = hotValue.toLong
//                rstList = TopTenActor.TopTenInfo(boardName, id, topic, hotRank) :: rstList
//            }
//
//        }
//      }
//      Right(rstList.reverse)
//    } catch {
//      case e: Exception =>
//        val msg = s"parseTopTen $url error: $e\ncontent: $content"
////        emailActor ! EmailActor.SendEmail("parseTopTen Bug", msg)
//        log.debug(msg)
//        Left(-1)
//    }
//  }
//
//  def genSmthImgUrl(uri: String) = "http:" + uri
//
//  def genBoardUrl(boardName: String, page: Int = 1): String = s"http://m.newsmth.net/board/$boardName/0?p=$page"
//
//  def genPostUrl(boardName: String, postId: Long): String = s"http://m.newsmth.net/article/$boardName/single/$postId/0"
//
//  def genWebPostUrl(boardName: String, postId: Long): String = s"http://www.newsmth.net/nForum/article/$boardName/$postId?ajax"
//
//  def getTopTenUrl: String = "http://m.newsmth.net/"
//
//  def loginAndGetCookie(id: String, psw: String,proxyOption: Option[ProxyInfo]): Future[Either[String, CookieStore]] = {
//    val method = new HttpPost("http://www.newsmth.net/nForum/user/ajax_login.json")
//
//    method.setHeaders(pcHeaders)
//    if (proxyOption.nonEmpty) {
//      val proxy = proxyOption.get
//
//      val httpHost = new HttpHost(proxy.ip, proxy.port)
//      val config = RequestConfig.custom()
//        .setProxy(httpHost)
//        .setConnectTimeout(10000)
//        .setSocketTimeout(10000)
//        .setRedirectsEnabled(false)
//        .setCircularRedirectsAllowed(false)
//        .setCookieSpec(CookieSpecs.DEFAULT)
//        .build()
//      method.setConfig(config)
//    }
//    val parameters = List(new BasicNameValuePair("id", id), new BasicNameValuePair("passwd", psw)).asJava
//    method.setEntity(new UrlEncodedFormEntity(parameters))
//
//
//
//    Future {
//      try {
//        val cookieStore: CookieStore = new BasicCookieStore()
//        val clientContext: HttpClientContext = new HttpClientContext()
//        clientContext.setCookieStore(cookieStore)
//        val response = HttpClientUtil.httpClient.execute(method, clientContext)
//        EntityUtils.consume(response.getEntity)
//        response.close()
//        if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
//          Right(cookieStore)
//        } else {
//          log.debug(s"login error ,the response is $response")
//          Left("error")
//        }
//      } catch {
//        case ex: Exception =>
//          log.info("登录异常：" + ex)
//          Left(ex.toString)
//      }
//    }
//  }
//
//  def getUserInfo(userId: String, proxy: Option[String]) = {
//    val url="http://www.newsmth.net/nForum/user/query/"+userId+".json"
//    HttpClientUtil.fetch(url, proxy, Some(pcHeaders),None)
//  }
//
//  def getLoginImage(imgUrl: String, cookieStore: Option[CookieStore], proxy: Option[String] = None) = {
//    HttpClientUtil.fetchImg(imgUrl, proxy, None, cookieStore)
//  }
//
//  object PcUtil {
//
//    case class Like(score: Int, user: String, msg: String, time: Long)
//
//    case class Likes(userCount: Int, likeList: List[Like])
//
//    /*
//     * Either[Int, Likes] 解析成功返回Likes，出错返回errCode: Int.
//     * errCode定义根据具体情况明确一下。
//     */
//    def parseLike(content: String, url: String): Either[Int, Likes] = {
//      val doc = Jsoup.parseBodyFragment(content)
//      val body = doc.body()
//      try {
//        val contentCorner = body.getElementsByClass("b-content").get(0)
//        val wrapCorner = contentCorner.getElementsByClass(s"a-wrap").get(0)
//        val articleTable = wrapCorner.getElementsByClass("article").get(0)
//        val tableBody = articleTable.getElementsByClass("a-body").get(0)
//        val articleContent = tableBody.getElementsByClass("a-content").get(0)
//        val likesOp = articleContent.getElementsByClass("likes")
//        if (likesOp.isEmpty) {
//          Right(Likes(0, List()))
//        } else {
//          val likes = likesOp.get(0)
//          val divs = likes.getElementsByTag("div")
//          val likeName = divs.get(1)
//          val likeList = divs.get(2)
//          val countRegex = "有(\\d+)位用户评价了这篇文章：".r
//          val ul = likeList.getElementsByTag("ul").get(0)
//          val lis = ul.getElementsByTag("li")
//
//          var ls = List[Like]()
//          for (i <- 0 until lis.size()) {
//            val li = lis.get(i)
//            val spans = li.getElementsByTag("span")
//            val scoreRegex = "\\[(.*)\\]".r
//            spans.get(0).text() match {
//              case scoreRegex(score) =>
//                val likeScore = score match {
//                  case "  " => 0
//                  case x => x.toInt
//                }
//                val likeUser = spans.get(1).text().dropRight(1)
//                val likeMsg = spans.get(2).text()
//                val time = spans.get(3).text().drop(1).dropRight(1)
//                val likeTime = TimeUtil.date2TimeStamp4Likes(time)
//                ls = Like(likeScore, likeUser, likeMsg, likeTime) :: ls
//              case _ =>
//                log.debug(s"ParseLike error: likeScore ${spans.get(0).text()} cannot match scoreRegex!!!")
//            }
//          }
//
//          likeName.text() match {
//            case countRegex(count) =>
//              val userCount = count.toInt
//              Right(Likes(userCount, ls.reverse))
//            case _ =>
//              val msg = s"ParseLike error: likeName ${likeName.text()} cannot match countRegex!!!"
//              emailActor ! EmailActor.SendEmail("ParseLike Bug", msg)
//              log.debug(msg)
//              Left(-1)
//          }
//        }
//      } catch {
//        case e: Exception =>
//          val msg = s"parseLike $url error: $e\ncontent: $content"
////          emailActor ! EmailActor.SendEmail("ParseLike Bug", msg)
//          log.debug(msg)
//          Left(-1)
//      }
//
//    }
//    /*
//     * Either[Int, SlickTables.rPosts] 解析成功返回rPosts，出错返回errCode: Int.
//     * errCode定义根据具体情况明确一下。
//     * (1, ...)指定的文章不存在或链接错误
//     * (-1, ...)其他错误导致解析失败
//     */
//    def parseMainPost(content: String, url: String): Either[Int, SlickTables.rPosts] = {
//      try {
//        val boardName = url.split("/").takeRight(2).head
//        val doc = Jsoup.parseBodyFragment(content)
//        val body = doc.body()
//        val contentCorner = body.getElementsByClass("b-content").get(0)
//        val error = contentCorner.getElementsByClass("error")
//        if (error.size() != 0) {
//          val errMsg = error.get(0).getElementsByTag("li").text()
//          if (errMsg == "指定的文章不存在或链接错误") {
//            Left(1)
//          } else {
//            Left(-1)
//          }
//        } else {
//          val wrapCorner = contentCorner.getElementsByClass(s"a-wrap").get(0)
//          val articleTable = wrapCorner.getElementsByClass("article").get(0)
//          val tableBody = articleTable.getElementsByClass("a-body").get(0)
//          val articleContent = tableBody.getElementsByClass("a-content").get(0)
//          val articleContentHtml = articleContent.html()
//          val contentHead = articleContentHtml.drop(3).split("<br>").take(4)
//          val nameLine = if (contentHead(0).startsWith("发信人")) contentHead(0) else contentHead(0).split(">").last
//          val titleLine = contentHead(1).replace("&nbsp;", " ")
//          val timeLine = if (contentHead(2).startsWith(" 发信站")) {
//            contentHead(2)
//          } else {
//            contentHead(3)
//          }
//          val regex1 = "\\S信人:\\s(.*)\\s[(](.*)[)],?[\\s\\S]*".r
//          val regex2 = "\\s标  题:\\s(.*)".r
//          val regex3 = "\\s发信站:(.*)[(](.*)[)](.*)".r
//          nameLine match {
//            case regex1(an, _) =>
//              val authorId = an
//              titleLine match {
//                case regex2(t) =>
//                  val title = t.stripSuffix(" ").take(126)
//                  timeLine match {
//                    case regex3(_, time, _) =>
//                      val timestamp = genTimestamp(time)
//                      val contentTail = articleContent.getElementsByTag("font")
//                      val ipLine = try {
//                        Some(contentTail.get(contentTail.size() - 2))
//                      } catch {
//                        case e: Exception =>
//                          None
//                      }
//                      val ipRegex = """((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d))))""".r
//                      val ip = if (ipLine.nonEmpty) ipRegex.findFirstIn(ipLine.get.text()).getOrElse("") else ""
//                      val images = articleContent.getElementsByTag("img")
//                      var imgs = ""
//                      var imgList = List[String]()
//                      var imgListWithBr = List[String]()
//
//                      images.forEach {
//                        i =>
//                          val src = i.attr("src")
//                          val tmpArray = src.split("/")
//                          if (src.startsWith("//att.newsmth.net/nForum/att/")) {
//                            val img = "http://att.newsmth.net/" + tmpArray.drop(3).dropRight(1).mkString("/")
//                            imgs = "<br><img src=\'" + img + "\'>"
//                            imgList = img :: imgList
//                            imgListWithBr = imgs :: imgListWithBr
//                          }
//                          else if (src.startsWith("/nForum/att/")) {
//                            val img = ("http://www.newsmth.net" + src).dropRight(6)
//                            imgs = "<br><img src=\'" + img + "\'>"
//                            imgList = img :: imgList
//                            imgListWithBr = imgs :: imgListWithBr
//                          }
//                      }
//                      val contentHtmlWithLike = articleContentHtml.split("<br>").drop(4).mkString("<br>")
//                      val contentHtml = contentHtmlWithLike.split("<div class=\"likes\">")(0).split("<div class=\"t-pre-bottom t-btn\"")(0).dropRight(5)
////                      val contentText = contentHtml.split("--").headOption.getOrElse("") + imgListWithBr.mkString
//                      val contentText =
//                        if(contentHtml.split("--").lengthCompare(1) == 0) { //无文本，仅有图片的情况下，帖子内没有 -- 标记
//                          contentHtml.split("<font class=")(0) + imgListWithBr.mkString
//                        } else {
//                          contentHtml.split("--").dropRight(1).mkString("--") + imgListWithBr.mkString
//                        }
//                      val topicId = url.split("/").last.dropRight(5)
//                      val boardNameCn = boardNameMap(boardName)
//                      Right(SlickTables.rPosts(
//                            id = 0L,
//                            topicId = topicId.toLong,
//                            postId = topicId.toLong,
//                            isMain = 1,
//                            title = title,
//                            authorId = authorId,
//                            contentHtml = contentHtml,
//                            contentText = contentText,
//                            imgs = imgList.reverse.mkString(";"),
//                            hestiaImgs = "",
//                            postTime = timestamp,
//                            boardName = boardName,
//                            url = url,
//                            ip = ip,
//                            boardNameCn = boardNameCn
//                          ))
//
//                    case other =>
//                      val msg = s"parseMainPost $url timeLine: $other"
//                      emailActor ! EmailActor.SendEmail("parseMainPost Bug", msg)
//                      log.error(msg)
//                      Left(-1)
//                  }
//
//                case other =>
//                  val msg = s"parseMainPost $url titleLine: $other"
//                  emailActor ! EmailActor.SendEmail("parseMainPost Bug", msg)
//                  log.error(msg)
//                  Left(-1)
//
//              }
//
//            case other =>
//              val msg = s"parseMainPost $url nameLine: $other"
//              emailActor ! EmailActor.SendEmail("parseMainPost Bug", msg)
//              log.error(msg)
//              Left(-1)
//
//          }
//        }
//      } catch {
//        case e: Exception =>
//          val msg = s"parseMainPost $url error: $e\ncontent:$content"
////          emailActor ! EmailActor.SendEmail("parseMainPost Bug", msg)
//          log.error(msg)
//          e.printStackTrace()
//          Left(-1)
//      }
//    }
//
//    def genTimestamp(time: String) = {
//      val timeTmp = time.replace("&nbsp;&nbsp;", " ")
//      val timestamp = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH).parse(timeTmp).getTime
//      timestamp
//    }
//
//  }
//
//
//    def main1(args: Array[String]): Unit = {
//      import PcUtil._
//      val url = "http://www.newsmth.net/nForum/article/Age/18413264?ajax"
//      HttpClientUtil.fetch(url, None, Some(pcHeaders)).map {
//        case Right(str) =>
//          parseMainPost(str, url) match {
//            case Right(x) =>
//              println(s"result: ${x.contentText}")
////              x.foreach(t=>println(t))
//            case Left(e) => log.error(s"error: $e")
//          }
//
//        case Left(e) =>
//          log.info(s"ParseMainPost error: $e")
//          Nil
//      }
//    }
//
//
//
//
//}
