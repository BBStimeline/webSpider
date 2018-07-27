package com.neo.sk.webSpider.utils

import java.io._

import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util
import java.util.Locale

import akka.http.scaladsl.model.headers.Date
import com.neo.sk.webSpider.models.SlickTables
import com.neo.sk.webSpider.models.dao.IssueDao
import org.slf4j.LoggerFactory
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable
/**
  * User: sky
  * Date: 2018/7/20
  * Time: 17:59
  */
object MuseClient {

  def parseIssueList(content:String)={
    val doc = Jsoup.parse(content)
    try {
      val list=doc.getElementById("available_issues_list_text").getElementsByTag("a")
      val l=list.forEach{r=>
        val url=r.attr("href").split("/")(2)
        val title=r.text()
        IssueDao.addInfo(SlickTables.rIssues(url,title))
        println((url,title))
      }
    }catch {case e:Exception=>
      println(e.getMessage)
    }
  }

  def parseArticleList(content:String)={
    val doc = Jsoup.parse(content)
    var issueList = List[String]()
    try {
      val list=doc.getElementById("articles_list_wrap").select("div .card").select(".row").select(".small-30")
      list.forEach{r=>
        issueList= r.getElementsByTag("a").first().attr("href") ::issueList
      }
      issueList.reverse
    }catch {case e:Exception=>
      println(e.getMessage)
      Nil
    }
  }

  def parseArticleFull(content:String)={
//    val t1=System.currentTimeMillis()
    val doc = Jsoup.parse(content)
    try {
      var title=""
      var abs=""
      var authors=List[String]()
      var authorInfo=List[String]()
      var index=List[String]()
      var fullText=""
      var mail=List[String]()
      var page=""
      var doi=""
      var articleType=""
      val mainDiv=doc.getElementsByClass("card_text").first().getElementsByTag("ul").first()
      try{
        title+=mainDiv.getElementsByClass("title").text()
        if(title==""){
          title+=doc.getElementById("front").getElementsByClass("card_text").first().getElementsByTag("ul").first()
            .getElementsByClass("title").first().getElementById("article-title").text()
        }
      }catch {case e:Exception=>
        println("title"+e.getMessage)
      }
      try{
        if(doc.getElementsByClass("abstract").first().getElementById("body")==null){
          abs=doc.getElementsByClass("abstract").first().getElementsByTag("p").first().text()
        }else{
          doc.getElementsByClass("abstract").first().getElementById("body").
            getElementsByTag("p").forEach(r=>abs+=r.text())
        }
      }catch {case e:Exception=>
        println("abs"+e.getMessage)
      }
      try{
        try {
          mainDiv.getElementsByClass("authors").first().getElementsByTag("a").forEach(r=>authors= r.text() ::authors)
        }catch {case e:Exception=>
          println("author1"+e.getMessage)
        }
        if(authors.isEmpty){
          try {
            authors=List(doc.getElementById("front").getElementsByClass("card_text").first().getElementsByTag("ul").first()
              .getElementsByClass("author").first().getElementsByTag("a").first().text()
            )
          }catch {case e:Exception=>
            println("author2"+e.getMessage)
          }
        }
      }catch {case e:Exception=>
        println("author"+e.getMessage)
      }
      try {
        if(doc.getElementById("back")!=null){
          authorInfo=List(doc.getElementById("back").getElementsByClass("bio").first().getElementsByTag("p").first().ownText())
        }
      }catch {case e:Exception=>
        println("authorInfo"+e.getMessage)
      }
      try{
        page=mainDiv.getElementsByClass("pg").first().text()
      }catch {case e:Exception=>
        println("index"+e.getMessage)
      }
      try{
        doi=mainDiv.getElementsByClass("doi").first().text()
      }catch {case e:Exception=>
        println("doi"+e.getMessage)
      }
      try{
        articleType=mainDiv.getElementsByClass("type").first().text()
      }catch {case e:Exception=>
        println("articleType"+e.getMessage)
      }
//      println(title,authors.reverse,authorInfo.reverse,mail.reverse,abs,index.reverse,page)
//      println(System.currentTimeMillis()-t1)
//      println(title)
//      println(authors.reverse)
//      println(authorInfo.reverse)
//      println(mail.reverse)
//      println(abs)
//      println(index.reverse)
//      println(page)
//      println(doi)
//      println(articleType)
      //
      SlickTables.rArticles("","",title,authors.mkString("%"),authorInfo.mkString("||"),mail.mkString(";"),page,abs,index.mkString(";"),fullText,"",articleType,doi,3,"")
    }catch {case e:Exception=>
      println(e.getMessage)
      SlickTables.rArticles().copy(isDone = 3)
    }
  }

  def getIssueList(filePath:String) = {
    val file = new File(filePath)
    if (file.isFile && file.exists) {
      try {
        val in = new FileInputStream(filePath)
        val inReader = new InputStreamReader(in, "UTF-8")
        val bufferedReader = new BufferedReader(inReader)
        var write=List[(String,String)]()
        bufferedReader.lines().forEach { l =>
          val t=l.split(",")
          write = (t(0),t(1)) :: write
        }
        write
      } catch {
        case e: Exception =>
          println("get exception:" + e.getStackTrace)
          Nil
      }
    }else{
      println(s"file--$filePath isn't exists.")
      Nil
    }
  }

}
