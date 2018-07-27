package com.neo.sk.webSpider.core

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import com.neo.sk.webSpider.Boot.{executor, scheduler, spiderManager, timeout}
import com.neo.sk.webSpider.common.AppSettings
import com.neo.sk.webSpider.core.ArticleActor.StartArticle
import com.neo.sk.webSpider.models.SlickTables
import com.neo.sk.webSpider.models.dao.{ArticleDao, IssueDao}
import com.neo.sk.webSpider.utils.{MuseClient, SeleniumClient, TimeUtil}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
/**
  * User: sky
  * Date: 2018/7/17
  * Time: 10:15
  */
object IssueActor {
  val log = LoggerFactory.getLogger(this.getClass)
  trait Command
  private final case object TimeOutKey
  case object TimeOut extends Command
  final case class ChildDead(name:String,childRef:ActorRef[ArticleActor.Command]) extends Command
  private case class SwitchBehavior(name: String, behavior: Behavior[Command]) extends Command
  case class StartIssue(link: String) extends Command
  case class AddArticleList(list: List[String]) extends Command
  case object AddUndoArticleList extends Command

  def init(url: String,issue:String): Behavior[Command] = {
    Behaviors.setup[Command] { ctx =>
      log.debug(s"spiderActor--$url is starting")
      implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
      Behaviors.withTimers[Command] { implicit timer =>
        val hash: mutable.Queue[String] = mutable.Queue()
        idle(url,issue)(hash)
      }
    }
  }

  private def idle(url:String,issue:String)(hash: mutable.Queue[String] = mutable.Queue())(implicit stashBuffer: StashBuffer[Command], timer: TimerScheduler[Command]): Behavior[Command] = {
    Behaviors.immutable[Command] { (ctx, msg) =>
      msg match {
        case msg:StartIssue=>
          println(msg)
          Future{
            val content=SeleniumClient.fetch("http://muse.jhu.edu/issue/"+msg.link)
            val list=MuseClient.parseArticleList(content)
            ctx.self ! AddArticleList(list)
          }
          Behaviors.same

        case msg:AddArticleList=>
          msg.list.map{r=>
            hash.enqueue(r)
            ArticleDao.addInfo(SlickTables.rArticles(id = r,issue = issue,issueId = url))
          }
          for(i<-0 to 2){
            val a=hash.dequeue()
            getArticleActor(ctx,issue,url,a) ! StartArticle("http://muse.jhu.edu"+a)
          }
          Behaviors.same

        case AddUndoArticleList=>
          ArticleDao.getUndoListByIssue(url).map{ ls=>
            ls.foreach{ r=>
              hash.enqueue(r.id)
            }
            println(s"issue${url} --article count=${ls.size}")
            for(i<-0 to 1){
              val a=hash.dequeue()
              getArticleActor(ctx,issue,url,a) ! StartArticle("http://muse.jhu.edu"+a)
            }
          }
          Behaviors.same

        case msg:ChildDead=>
          log.info(s"${msg.name} is dead")
          val a=hash.dequeue()
          getArticleActor(ctx,issue,url,a) ! StartArticle("http://muse.jhu.edu"+a)
          if(hash.isEmpty){
            println(s"issue-$url is stopping")
            Behaviors.stopped
          }
          Behaviors.same

        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
      }
    }

  private def busy(msgLs: List[Command]): Behavior[Command] =
    Behaviors.immutable[Command] { (ctx, msg) =>
      msg match {
        case SwitchBehavior(_, b) =>
          msgLs.reverse.foreach(ctx.self.tell)
          b
        case otherMsg =>
          busy(otherMsg :: msgLs)
      }
    }

  private def getArticleActor(ctx: ActorContext[Command],issue:String,issueId:String, id:String) = {
    val childName = s"articleActor-${id.replace("/","-")}"
    println(childName)
    ctx.child(childName).getOrElse {
      val actor=ctx.spawn(ArticleActor.init(issue,issueId,id), childName)
      ctx.watchWith(actor,ChildDead(childName,actor))
      actor
    }.upcast[ArticleActor.Command]
  }
}

