package com.neo.sk.webSpider.core

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import com.neo.sk.webSpider.Boot.{executor, scheduler, spiderManager, timeout}
import com.neo.sk.webSpider.common.AppSettings
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
  case class AddArticle(unit: SlickTables.rArticles) extends Command
  case class UpdateArticle(unit: SlickTables.rArticles) extends Command
  def init(issue:String,issueId:String, dataId: String): Behavior[Command] = {
    Behaviors.setup[Command] { ctx =>
      implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
      log.info(s"articleActor--$dataId is starting")
      Behaviors.withTimers[Command] { implicit timer =>
        idle(issue,issueId,dataId)
      }
    }
  }

  private def idle(issue:String,issueId:String,id:String)(implicit stashBuffer: StashBuffer[Command], timer: TimerScheduler[Command]): Behavior[Command] = {
    Behaviors.immutable[Command] { (ctx, msg) =>
      msg match {
        case msg:StartArticle=>
          println(msg)
          /*Future{
            val content=HttpClientUtil.fetch(msg.link,None,None,None)
            val r=MuseClient.parseArticleFull(content)
            ctx.self ! AddArticle(r)
          }*/
          HttpClientUtil.fetch(msg.link,None,None,None).map{
            case Right(t)=>
              val r=EducationClient.parseArticleFull(t)
              ctx.self ! AddArticle(r)
            case Left(e)=>
              println(e)
          }

          timer.startSingleTimer(TimeOutKey,TimeOut,2.minutes)
          Behaviors.same

        case msg:AddArticle=>
          ArticleDao.updateInfo(msg.unit.copy(id = id,issue = issue,issueId = issueId)).map(r=>
            if(r<=0) log.debug(s"ArticleDao.updateInfo error id=$id")
          )
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
