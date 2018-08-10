package com.neo.sk.webSpider.core

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import com.neo.sk.webSpider.Boot.{executor, proxyActor, scheduler, spiderManager, timeout}
import com.neo.sk.webSpider.common.AppSettings
import com.neo.sk.webSpider.core.ArticleActor.{StartArticle, StartRefArticle}
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
  * Date: 2018/8/8
  * Time: 19:51
  */
object VolumeActor {
  val log = LoggerFactory.getLogger(this.getClass)
  trait Command
  private final case object TimeOutKey
  case object TimeOut extends Command
  private case class SwitchBehavior(name: String, behavior: Behavior[Command]) extends Command
  case class StartVolume(link: String,id:String,title:String) extends Command

  def init(url: String): Behavior[Command] = {
    Behaviors.setup[Command] { ctx =>
      log.debug(s"spiderActor--$url is starting")
      println(s"startIssue--$url")
      implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
      Behaviors.withTimers[Command] { implicit timer =>
        idle(url)
      }
    }
  }

  private def idle(url:String)(implicit stashBuffer: StashBuffer[Command], timer: TimerScheduler[Command]): Behavior[Command] = {
    Behaviors.immutable[Command] { (ctx, msg) =>
      msg match {
        case msg:StartVolume=>
          val future:Future[Option[String]]=proxyActor ? (GetProxy(_))
          future.map{r=>
            HttpClientUtil.fetch("https://www.tandfonline.com"+msg.link,r,None,None).map{
              case Right(t) =>
                EducationClient.parseIssueList(t,msg.id,msg.title)
                IssueDao.updateVolume("https://www.tandfonline.com"+msg.link)
                Behaviors.stopped
              case Left(e) =>
                log.info("--- limit")
                timer.startSingleTimer(TimeOutKey,msg,30.seconds)
            }
          }
          Behaviors.same
      }
    }
  }
}
