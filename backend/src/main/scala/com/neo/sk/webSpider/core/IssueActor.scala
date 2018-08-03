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
  case class AddArticleList(list: List[(String,String,String)]) extends Command
  case object AddUndoArticleList extends Command
  val baseUrl="https://www.tandfonline.com"

  def init(url: String,issue:String): Behavior[Command] = {
    Behaviors.setup[Command] { ctx =>
      log.debug(s"spiderActor--$url is starting")
      println(s"startIssue--$url")
      implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
      Behaviors.withTimers[Command] { implicit timer =>
        val hash: mutable.Queue[String] = mutable.Queue()
        ArticleDao.getArticleByIssue(url).map{r=>
          ctx.self ! SwitchBehavior("idle",idle(url,issue,r.toSet)(hash))
        }
//        idle(url,issue,Set.empty)(hash)
        busy(Nil)
      }
    }
  }

  private def idle(url:String,issue:String,hasDone:Set[String])(hash: mutable.Queue[String] = mutable.Queue())(implicit stashBuffer: StashBuffer[Command], timer: TimerScheduler[Command]): Behavior[Command] = {
    var count1=0
    var count2=0
    Behaviors.immutable[Command] { (ctx, msg) =>
      msg match {
        case msg:StartIssue=>
          /*Future{
            val content=SeleniumClient.fetch("https://www.tandfonline.com"+msg.link)
            val list=EducationClient.parseArticleList(content)
            ctx.self ! AddArticleList(list)
          }*/
          val future:Future[Option[String]]=proxyActor ? (GetProxy(_))
          future.map{r=>
            HttpClientUtil.fetch(baseUrl+msg.link,r,None,None).map{
              case Right(t) =>
                val list=EducationClient.parseArticleList(t)
                ctx.self ! AddArticleList(list)
              case Left(e) =>
                log.info(url+"--- limit")
                timer.startSingleTimer(TimeOutKey,msg,30.seconds)
            }
          }
          Behaviors.same

        case msg:AddArticleList=>
          println(url+s"-done-${hasDone.size}-notDone--${msg.list.size-hasDone.size}")
          msg.list.map{r=>
            if(!hasDone.contains(r._1)){
              hash.enqueue(r._1)
              ArticleDao.addInfo(SlickTables.rArticles(id = r._1,page= r._2,issue = issue,fulltext = r._3,issueId = url,union = 1))
            }
          }
          if(msg.list.size>hasDone.size){
            IssueDao.updateIssue(url)
            val t=math.min(7,hash.size-1)
            for(i<-0 to t){
              val a=hash.dequeue()
              getArticleActor(ctx,issue,url,a) ! StartArticle(baseUrl+a)
            }
            Behaviors.same
          }else{
            println(s"issue-$url is stopping")
            Behaviors.stopped
          }

        case AddUndoArticleList=>
          ArticleDao.getUndoListByIssue(url).map{ ls=>
            ls.foreach{ r=>
              hash.enqueue(r.id)
            }
            println(s"issue${url} --article count=${ls.size}")
            count1=ls.size
            val t=math.min(5,hash.size-1)
            for(i<-0 to t){
              val a=hash.dequeue()
//              getArticleActor(ctx,issue,url,a) ! StartRefArticle(baseUrl+a.replace("doi/abs", "doi/ref").replace("doi/full", "doi/ref"))
              getArticleActor(ctx,issue,url,a) ! StartArticle(baseUrl+a)
            }
          }
          Behaviors.same

        case msg:ChildDead=>
          log.info(s"${msg.name} is dead")
          count2+=1
          if(hash.isEmpty){
            if(count1==count2){
              log.info(s"$url-count-$count1--$count2")
              println(s"issue-$url is stopping")
              Behaviors.stopped
            }else{
              log.info(s"$url-count-$count1--$count2")
              Behaviors.same
            }
          }else{
            val a=hash.dequeue()
            getArticleActor(ctx,issue,url,a) ! StartArticle(baseUrl+a)
            Behaviors.same
          }


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
    val childName = s"articleActor-${id.replace("/","-").replace("?","-").replace("=","-")}"
    log.info(childName)
    ctx.child(childName).getOrElse {
      val actor=ctx.spawn(ArticleActor.init(issue,issueId,id), childName)
      ctx.watchWith(actor,ChildDead(childName,actor))
      actor
    }.upcast[ArticleActor.Command]
  }
}

