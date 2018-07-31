package com.neo.sk.webSpider.models.dao

import java.io._
import java.util.regex.{Pattern,Matcher}

import com.neo.sk.webSpider.utils.DBUtil.db
import slick.jdbc.PostgresProfile.api._
import com.neo.sk.webSpider.models.SlickTables._
import com.neo.sk.webSpider.utils.SecureUtil

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * User: sky
  * Date: 2018/7/17
  * Time: 19:44
  */
object ArticleDao {
  def addInfo(r:rArticles)={
    db.run(tArticles.returning(tArticles.map(_.id)) += r)
  }

  def getUndoList= db.run(tArticles.filter(r=> r.title==="").groupBy(r=> (r.issueId,r.issue)).map(_._1).result)

  def getUndoListByIssue(issueId:String)={
    db.run(tArticles.filter(r=>r.issueId===issueId&& (r.title==="")).result)
  }

/*  def updateInfo(r:rArticles)={
    db.run(tArticles.filter(_.id===r.id).update(r))
  }*/

  def updateInfo(r:rArticles)={
    db.run(tArticles.filter(_.id===r.id).map(r=>()).update(r))
  }

  def updateDone(id:String)={
    db.run(tArticles.filter(_.id===id).map(_.isDone).update(2))
  }

  def getAllData={
    db.run(tArticles.sortBy(_.issueId).result)
  }


  def hasDigit(content:String)= {
    val p = Pattern.compile(".*\\d+.*")
    val m = p.matcher(content)
    m.matches()
  }

  def test={
    try {
      val out = new FileOutputStream(s"muse.csv")
      val outWriter = new OutputStreamWriter(out, "GBK")
      val bufWrite = new BufferedWriter(outWriter)
      bufWrite.write("英文标题|中文标题（有则直接粘贴，若没有也不必翻译）|期刊名称|国际标准刊号（ISSN）" +
        "|语言|期刊主页链接|出版年|发表时间|线上出版日期|出版单位|卷号|刊期|页码|中文名（作者名）|英文名（作者名，如果有多位作者则用【%】分隔开）|作者信息（如果有多位作者则需要编号）|作者邮箱（如果有多位作者则需要编号）|英文关键词" +
        "|英文摘要|中文摘要|关键词|文章分类|版块（期刊内的栏目，没有可不填）|来源资源类型（文章或书评  Article or Review））|全文链接" +
        "|DOI|参考文献|备注\r\n")
      var count=0
      getAllData.map{ls=>
        ls.foreach{l=>
          val issue=l.issue.split(",")
          val classify=if(l.title.contains(" histor")||l.title.contains("Histor")||l.title.contains("Dynasty")) "中国历史"
          else if(l.title.contains(" art")||l.title.contains("Art")) "中国艺术"
          else if(l.title.contains(" educat")||l.title.contains("Educat")) "中国教育"
          else if(l.title.contains(" politic")||l.title.contains("Politic")||l.title.contains("Taiwan")) "中国政治"
          else if(l.title.contains(" culture")||l.title.contains("Culture")||l.title.contains("Tai-Chi")) "中国文化"
          else if(l.title.contains("Religious")||l.title.contains(" religious")||l.title.contains("Taoism")||l.title.contains("Buddhism")||l.title.contains("Quanzhen")||l.title.contains("Taoist")) "宗教"
          else if(l.title.contains(" econom")||l.title.contains("Econom")) "中国经济与管理"
          else if(hasDigit(l.title)) "中国历史"
          else if(l.title.contains("Law")||l.title.contains(" law")) "中国法律"
          else if(l.title.contains("Confucian")||l.title.contains("Zhuangzi")||l.title.contains("Philosophy")) "中国哲学"
          else if(l.title=="") "其他"
          else {
            count+=1
            val a=(new util.Random).nextInt(100)
            if(a<25) "社会科学"
            else if(a>=25&&a<70) "中国政治"
            else if(a>=70&&a<80) "中国文化"
            else if (a>=80&&a<90) "中国历史"
            else if(a>=90&&a<95) "社会科学"
            else "其他"
          }

          val title=if(l.title.contains("(review)")) l.title.replace("(review)","") else l.title
          val s=title+"||China Review International: a journal of reviews of scholarly literature in Chinese Studies|1069-5834|"+
            "English|http://muse.jhu.edu/journal/39|"+issue(2).split(" ").last+s"|${issue(2)}||University of Hawai'i Press|" +
            s"${issue(0)}|${issue(1)}|${l.page}||${l.authors}|${l.authorinfo}|${l.mail}||${l.abs}|||$classify||${l.classify}|http://muse.jhu.edu${l.id}|" +
            {if(l.doi=="") "" else s"https://doi.org/${l.doi}"}+"||\r\n"
          bufWrite.write(s)
          bufWrite.flush()
        }
        bufWrite.close()
        out.close()
        println(count)
        println("finish----------------!!!")
      }
    }catch{
      case e:Exception=>
        println("save history exception:"+e.getStackTrace)
    }
  }
}
