package com.neo.sk.webSpider.core

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import com.neo.sk.webSpider.Boot.{executor, proxyActor, scheduler, spiderManager, timeout}
import com.neo.sk.webSpider.common.AppSettings
import com.neo.sk.webSpider.core.ProxyActor.GetProxy
import com.neo.sk.webSpider.models.SlickTables
import com.neo.sk.webSpider.models.dao.{ArticleDao, IssueDao}
import com.neo.sk.webSpider.utils._
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
/**
  * User: sky
  * Date: 2018/7/17
  * Time: 16:44
  */
object ArticleActor {
  val log = LoggerFactory.getLogger(this.getClass)
  trait Command
  private final case object TimeOutKey
  case object TimeOut extends Command

  case class StartArticle(link: String) extends Command
  case class StartRefArticle(link: String) extends Command
  case class AddArticle(unit: SlickTables.rArticles,t:Boolean) extends Command
  case class AddArticleRef(unit: String) extends Command
  case class UpdateArticle(unit: SlickTables.rArticles) extends Command
  var count=0
  var count1=0

  def init(issue:String,issueId:String, dataId: String): Behavior[Command] = {
    Behaviors.setup[Command] { ctx =>
      implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
      log.info(s"articleActor--$dataId is starting")
      println(s"startArticle--$dataId")
      Behaviors.withTimers[Command] { implicit timer =>
        idle(issue,issueId,dataId)
      }
    }
  }

  private def idle(issue:String,issueId:String,id:String)(implicit stashBuffer: StashBuffer[Command], timer: TimerScheduler[Command]): Behavior[Command] = {
    Behaviors.immutable[Command] { (ctx, msg) =>
      msg match {
        case msg:StartArticle=>
          val future:Future[Option[String]]=proxyActor ? (GetProxy(_))
          future.map { r =>
            HttpClientUtil.fetch(msg.link, r, None, None).map {
              case Right(t) =>
                count1+=1
                log.info(id + s"---success--$count1")
                val r = EducationClient.parseArticleFull(t,msg.link)
                if(r._2){
                  ArticleDao.updateInfo(r._1.copy(id = id,issue = issue,issueId = issueId,isDone = 4)).map { l =>
                    if (l <= 0) log.debug(s"ArticleDao.updateInfo error id=$id")
                    else {
                      count+=1
                      log.info(id + s"---success--$count--${count1-count}")
                      ctx.self ! AddArticle(r._1, r._2)
//                      ctx.self ! StartRefArticle(msg.link.replace("doi/abs", "doi/ref").replace("doi/full", "doi/ref"))
                    }
                  }
                }else{
                  ArticleDao.updateInfo(r._1.copy(id = id,issue = issue,issueId = issueId)).map { l =>
                    if (l <= 0) log.debug(s"ArticleDao.updateInfo error id=$id")
                    else {
                      count+=1
                      log.info(id + s"---success--$count--${count1-count}")
                      ctx.self ! AddArticle(r._1, r._2)
                    }
                  }
                }
              case Left(e) =>
                log.info(id + "--- limit")
                timer.startSingleTimer(TimeOutKey,msg,60.seconds)
            }
          }
          timer.startSingleTimer(TimeOutKey,TimeOut,5.minutes)
          Behaviors.same

        case msg:StartRefArticle=>
          val future:Future[Option[String]]=proxyActor ? (GetProxy(_))
          future.map { r =>
            HttpClientUtil.fetch(msg.link, r, None, None).map {
              case Right(t) =>
                val r = EducationClient.parseArticleRef(t)
                ArticleDao.updateRef(id,r).map { l =>
                  if (l <= 0){
                    log.debug(s"ArticleDao.updateInfo error id=$id")
                    Behaviors.stopped
                  } else{
                    count1+=1
                    log.info(id + s"---success--$count1")
                    ctx.self ! AddArticleRef(r)
                  }
                }

              case Left(e) =>
                if(e.contains("The URL has moved")){
                  count1+=1
                  log.info(id + s"---success--$count1")
                  ArticleDao.updateRef(id,"").map { l =>
                    if (l <= 0){
                      log.debug(s"ArticleDao.updateInfo error id=$id")
                      ctx.self ! AddArticleRef("")
                    } else{
                      count1+=1
                      log.info(id + s"---success--$count1")
                      ctx.self ! AddArticleRef("")
                    }
                  }
                }else{
                  log.info(id + "--- limit"+e)
                  timer.startSingleTimer(TimeOutKey,msg,60.seconds)
                }
            }
          }
          Behaviors.same

        case msg:AddArticle=>
          if(msg.t){
            /*ArticleDao.updateInfo(msg.unit.copy(id = id,issue = issue,issueId = issueId,isDone = 4)).map { r =>
              if (r <= 0) log.debug(s"ArticleDao.updateInfo error id=$id")
            }*/
            Behaviors.stopped
          }else{
           /* ArticleDao.updateInfo(msg.unit.copy(id = id,issue = issue,issueId = issueId)).map { r =>
              if (r <= 0) log.debug(s"ArticleDao.updateInfo error id=$id")
            }*/
            Behaviors.stopped
          }

        case msg:AddArticleRef=>
          /*ArticleDao.updateRef(id,msg.unit).map(r=>
            if(r<=0) log.debug(s"ArticleDao.updateInfo error id=$id")
          )*/
          Behaviors.stopped

        case TimeOut=>
          log.debug(s"article--$id--timeout")
          println(s"article--$id--timeout")
          ArticleDao.updateDone(id)
          Behaviors.stopped

        case msg:UpdateArticle=>
          ArticleDao.updateInfo(msg.unit.copy(id = id,issue = issue,issueId = issueId))
          Behaviors.stopped
      }
    }
  }
}
