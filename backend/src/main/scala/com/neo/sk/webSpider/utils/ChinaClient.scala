package com.neo.sk.webSpider.utils

import java.io._

import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util
import java.util.Locale

import akka.http.scaladsl.model.headers.Date
import com.neo.sk.webSpider.models.SlickTables
import org.slf4j.LoggerFactory
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable
/**
  * User: sky
  * Date: 2018/7/17
  * Time: 13:38
  */
object ChinaClient {

  def parseIssueList(content:String)={
    val doc = Jsoup.parse(content)
    var issueList = List[(String,String)]()
    try {
      val list=doc.select(".collection").select(".summary").get(0).getElementsByTag("a")
      val l=list.forEach{r=>
        issueList= (r.getElementsByTag("span").first().text(),"https://journals.openedition.org/chinaperspectives/"+r.attr("href")):: issueList
      }
      saveFile(issueList.map(r=>r._1+","+r._2).reverse.mkString("\n")+"\n")
      issueList
    }catch {case e:Exception=>
      println(e.getMessage)
      Nil
    }
  }

  def parseArticleList(content:String)={
    val doc = Jsoup.parse(content)
    var issueList = List[((String,String),String,String)]()
    try {
      val list=doc.getElementById("main").getElementsByTag("ul").first()
      val issueDiv=doc.getElementById("publiTitle").getElementsByTag("span")
      val issueTitle=(issueDiv.first().text(),issueDiv.last().text())
      list.getElementsByTag("a").forEach{r=>
        val aType=r.parent().parent().parent().parent().getElementsByTag("h2").first().text()
        issueList=(issueTitle,aType,r.attr("href"))::issueList
      }
      issueList.reverse
    }catch {case e:Exception=>
      println(e.getMessage)
      Nil
    }
  }

  def parseArticleFull(content:String)={
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
      try{
        title+=doc.getElementById("docTitle").text()
        title+="/"+doc.getElementById("docSubtitle").text()
      }catch {case e:Exception=>
        println("title"+e.getMessage)
      }
      try{
        doc.getElementById("abstract").getElementsByTag("p").forEach(r=>abs+=r.text())
      }catch {case e:Exception=>
        println("abs"+e.getMessage)
      }
      try {
        fullText=doc.getElementsByClass("fileinfo").first().parent().getElementsByTag("a").first().attr("href")
      }catch {case e:Exception=>
        println("fullText"+e.getMessage)
      }
      try{
        doc.getElementById("authors").getElementsByTag("h3").forEach(r=>authors= r.text() ::authors)
        doc.getElementById("authors").select("p").select(".description").forEach(r=>authorInfo=r.text()::authorInfo)
        mail=authorInfo.map{r=>
          r.substring(r.lastIndexOf("(")+1,r.lastIndexOf(")"))
        }
      }catch {case e:Exception=>
        println("author"+e.getMessage)
      }
      try{
        doc.getElementById("entries").getElementsByClass("index").first().getElementsByTag("a").forEach(r=>index=r.text()::index)
      }catch {case e:Exception=>
        println("index"+e.getMessage)
      }
      try{
        page=doc.getElementById("docPagination").text()
      }catch {case e:Exception=>
        println("index"+e.getMessage)
      }

//      println(title,authors.reverse,authorInfo.reverse,mail.reverse,abs,index.reverse,page)
//      println(title)
//      println(authors.reverse)
//      println(authorInfo.reverse)
//      println(mail.reverse)
//      println(abs)
//      println(index.reverse)
//      println(page)
//
//      SlickTables.rArticles("","",title,authors.mkString("%"),authorInfo.mkString("||"),mail.mkString(";"),page,abs,index.mkString(";"),fullText)
    }catch {case e:Exception=>
      println(e.getMessage)
//      SlickTables.rArticles()
    }
  }

  def getHtml(filePath:String) = {
    val file = new File(filePath)
    if (file.isFile && file.exists) {
      try {
        val in = new FileInputStream(filePath)
        val inReader = new InputStreamReader(in, "UTF-8")
        val bufferedReader = new BufferedReader(inReader)
        var write=""
        bufferedReader.lines().forEach(l=>
          write+=l
        )
        parseArticleList(write)
      } catch {
        case e: Exception =>
          println("get exception:" + e.getStackTrace)
      }
    }else{
      println(s"file--$filePath isn't exists.")
    }
  }

  def saveAppend(info: String) = {
    try {
      println(info)
      val fw = new FileWriter("articleList.txt", true)
      fw.write(info)
      fw.close()
    }catch{
      case e:Exception=>
        println("save history exception:"+e.getStackTrace)
    }
  }

  def saveFile(info: String) = {
    try {
      val out = new FileOutputStream(s"issueList.txt")
      val outWriter = new OutputStreamWriter(out, "UTF-8")
      val bufWrite = new BufferedWriter(outWriter)
      bufWrite.append(info)
      bufWrite.close()
      out.close()
    }catch{
      case e:Exception=>
        println("save history exception:"+e.getStackTrace)
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

  def main(args: Array[String]): Unit = {
//    getHtml("F:\\MyPython\\webSpider\\test\\write_dataIList.txt")
//    getHtml("F:\\MyPython\\webSpider\\test\\write_dataList.txt")
/*    try {
      val out = new FileOutputStream(s"title.csv")
      val outWriter = new OutputStreamWriter(out, "GBK")
      val bufWrite = new BufferedWriter(outWriter)
      //      bufWrite.write("标题|作者（英文名，多个作者名字中间用%隔开）|作者信息（工作单位、职务等）|作者邮箱|出版单位|期刊名称|卷号|期号|页码|出版年份|" +
      //        "DOI|issn|E-ISSN|文章类型|文章分类|关键词|中文关键词|英文摘要|中文摘要|参考文献|页面网址|语种|期刊网址\r\n")
      bufWrite.write("标题| 副标题\r\n")
      getAllData.map{ls=>
        ls.foreach{l=>
          var index=1
          //          var authorInfo=""
          //          l._1.authorinfo.split(".||").foreach{r=>
          //            authorInfo+=index.toString+r
          //            index+=1
          //          }
          //          val s=l._1.title+"|"+l._1.authors+"|"+l._1.authorinfo+"|"+
          //            l._1.mail+"|Centre d'étude français sur la Chine contemporaine|"+"China Perspectives||"+l._2.issue+"|"+l._1.page+"|"+
          //            l._2.issue.split("/")(0)+"||2070-3449|1996-4617||"+l._2.classfy+"|"+l._1.index+"||"+l._1.abs+"|||https://journals.openedition.org/chinaperspectives/"+l._2.id+"|English|http://chinaperspectives.revues.org\r\n"
          val s=if(l._2.classfy.contains("Book")) l._1.title.split("/")(0) else l._1.title
          bufWrite.write(s+"\r\n")
          bufWrite.flush()
        }
        bufWrite.close()
        out.close()
        println("finish----------------!!!")
      }
    }catch{
      case e:Exception=>
        println("save history exception:"+e.getStackTrace)
    }*/

  }

}
