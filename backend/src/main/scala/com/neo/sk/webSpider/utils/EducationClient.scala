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
        val id=url.split("#")(1)
        println(("https://www.tandfonline.com"+url,id,title))
        IssueDao.addVolume(SlickTables.rVolumes("https://www.tandfonline.com"+url,id,title))
      }
    }catch {case e:Exception=>
      println(e.getMessage)
    }
  }

  def parseIssueList(content:String,id:String,t:String)={
    val doc = Jsoup.parse(content)
    try {
      val list=doc.getElementsByClass("list-of-issues").first().getElementById(id).getElementsByTag("ul").first().getElementsByTag("a")
      val l=list.forEach{r=>
        val url=r.attr("href")
        val title=r.getElementsByTag("div").first().text()
        IssueDao.addInfo(SlickTables.rIssues(url,t+","+title,0,1))
        println((url,t+","+title,0,1))
      }
    }catch {case e:Exception=>
      println(e.getMessage)
    }
  }

  def parseArticleList(content:String)={
    val doc = Jsoup.parse(content)
    var issueList = List[(String,String,String)]()
    try {
      val list=doc.getElementsByClass("articleEntry")
      list.forEach{r=>
        val divTag=r.getElementsByClass("hlFld-Title").first().parent().getElementsByTag("a").first().attr("href")
        val page=r.getElementsByClass("tocPageRange").first().text()
        val date=r.getElementsByClass("tocEPubDate").first().getElementsByTag("span").first().ownText()
        issueList= (divTag,page,date)::issueList
      }
      issueList.reverse
    }catch {case e:Exception=>
      println(e.getMessage)
      Nil
    }
  }

  def parseArticleFull(content:String,url:String)={
    val doc = Jsoup.parse(content)
    try {
      //fullText ==  online date
      var title=""
      var subTitle=""
      var abs=""
      var authors=List[String]()
      var authorInfo=List[String]()
      var index=List[String]()
      var fullTextOnline=""
      var mail=List[String]()
      var page=""
      var doi=""
      var articleType=""
      var conRef=false

      if(url.contains("doi/full")){
        try{
          title=doc.getElementsByClass("NLM_article-title").first().text()
        }catch {case e:Exception=>
          println("title1"+e.getMessage)
        }
        try{
          abs+=doc.getElementsByClass("abstractSection").get(0).text()
        }catch {case e:Exception=>
          println("abs"+e.getMessage)
        }
      }else{
        try{
          title=doc.select("meta[name=dc.Title]").get(0).attr("content")
        }catch {case e:Exception=>
          println("title"+e.getMessage)
        }
        try{
          abs+=doc.select("meta[name=dc.Description]").get(0).attr("content")
        }catch {case e:Exception=>
          println("abs"+e.getMessage)
        }
      }
      try{
        subTitle=doc.getElementsByClass("sub-title").first().text()
      }catch {case e:Exception=>
        println("subTitle"+e.getMessage)
      }

      try{
        try {
          val mainDiv=doc.getElementsByClass("hlFld-ContribAuthor").first()
          mainDiv.getElementsByClass("entryAuthor").forEach{r=>
            authors=r.ownText()::authors
            try {
              var overTitle=r.getElementsByClass("overlay").first().text()
              if(overTitle=="View further author information"){
                val id=r.getElementsByClass("overlay").first().getElementsByClass("author-extra-info").first().attr("data-authorsinfo").split("\"")(3)
                overTitle=doc.getElementById(id).getElementsByTag("span").first().ownText()
              }
              authorInfo=overTitle::authorInfo
            }catch {case e:Exception=>
              println("authorInfo"+e.getMessage)
            }
          }
        }catch {case e:Exception=>
          println("author1"+e.getMessage)
        }
      }catch {case e:Exception=>
        println("author"+e.getMessage)
      }

      try{
        doi=doc.getElementsByClass("dx-doi").first().text()
      }catch {case e:Exception=>
        println("doi"+e.getMessage)
      }

      try{
        articleType=doc.getElementsByClass("toc-heading").first().text()
      }catch {case e:Exception=>
        println("articleType"+e.getMessage)
      }

      try {
        doc.getElementsByClass("abstractKeywords").first().getElementsByTag("a").forEach{r=>
          index=r.text()::index
        }
      }catch {case e:Exception=>
        println("index"+e.getMessage)
      }

      try {
        conRef=doc.getElementsByClass("tab-nav").first().text().contains("References")
      }catch {case e:Exception=>
        println("conRef"+e.getMessage)
      }
//            println(title,authors.reverse,authorInfo.reverse,mail.reverse,abs,index.reverse,page)
//            println(title)
//            println(subTitle)
//            println(authors.reverse.mkString("%"))
//            println(makeList(authorInfo.reverse))
//            println(makeList(mail.reverse))
//            println(abs)
//            println(index.reverse)
//            println(page)
//            println(doi)
//            println(articleType)
//            println(conRef)
      //
      (SlickTables.rArticles("","",title,authors.reverse.mkString("%"),makeList(authorInfo.reverse),"",page,abs,index.reverse.mkString(";"),fullTextOnline,"",articleType,doi,3,"",1,content,subTitle),conRef)
    }catch {case e:Exception=>
      println(e.getMessage)
      (SlickTables.rArticles().copy(isDone = 3),false)
    }
  }

  def parseArticleRef(content:String)= {
    val doc = Jsoup.parse(content)
    var ref=List[String]()
    try {
      val mainDiv=doc.getElementsByClass("references").first().getElementsByTag("li")
      mainDiv.forEach{r=>
        val con=r.getElementsByTag("span").first().text()
        ref=con::ref
      }
//      println(ref.size)
//      println(makeList(ref))
      makeList(ref)
    }catch {case e:Exception=>
      println(e.getMessage)
      ""
    }
  }

  def makeList(list:List[String])={
    var a=1
    var con=""
    if(list.size>1){
      list.foreach{r=>
        con+="["+a.toString+"]."+r
        a+=1
      }
    }else{
      list.foreach{r=>
        con+=r
      }
    }
    con
  }
}
