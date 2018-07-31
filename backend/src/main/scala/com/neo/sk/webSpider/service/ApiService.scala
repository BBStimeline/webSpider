package com.neo.sk.webSpider.service

import akka.actor.Scheduler
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import org.slf4j.LoggerFactory
import com.neo.sk.webSpider.Boot.{executor, spiderManager}
import com.neo.sk.webSpider.common.AppSettings
import com.neo.sk.webSpider.core.SpiderManager
import com.neo.sk.webSpider.models.dao.{ArticleDao, IssueDao}
import com.neo.sk.webSpider.protocols.CommonProtocol.CommonRsp
import com.neo.sk.webSpider.shared.ptcl.SuccessRsp
import com.neo.sk.webSpider.utils.ChinaClient
/**
  * User: sky
  * Date: 2018/7/17
  * Time: 9:54
  */
trait ApiService extends ServiceUtils with SessionBase {
  import io.circe._
  import io.circe.generic.auto._

  implicit val timeout: Timeout

  implicit val scheduler: Scheduler

  case class AddIssueList(argType:Int,content:String)

  private val start=(path("start")&get){
    IssueDao.getUndoData.map{ls=>
      val list=ls.map(r=>(r.id,r.issue)).toList
      spiderManager ! SpiderManager.AddIssueList(list)
    }
//    ArticleDao.getUndoList.map{ ls=>
//      val list=ls.map(r=>(r._1,r._2)).toList.reverse
//      spiderManager ! SpiderManager.AddUndoIssueList(list)
//    }
    complete(SuccessRsp())
  }

  val apiRoutes: Route =
    pathPrefix("api") {
      start
    }
}
