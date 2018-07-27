package com.neo.sk.webSpider.utils

import java.util.concurrent.TimeUnit

import com.neo.sk.webSpider.models.SlickTables
import com.neo.sk.webSpider.models.dao.IssueDao
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.chrome.ChromeDriver
/**
  * User: sky
  * Date: 2018/7/21
  * Time: 19:19
  */
object EducationClient {
  def parseVolumeList(content:String)={
    val doc = Jsoup.parse(content)
    try {
      val list=doc.getElementsByClass("list-of-issues").first().getElementsByTag("li")
      println(list.size())
      val l=list.forEach{r=>
        val url=r.getElementsByTag("a").first().attr("href")
        val title=r.text()
        println((url,title))
      }
    }catch {case e:Exception=>
      println(e.getMessage)
    }
  }

  def parseIssueList(content:String,id:String)={
    val doc = Jsoup.parse(content)
    try {
      val list=doc.getElementsByClass("list-of-issues").first().getElementById(id).getElementsByTag("a")
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
}
