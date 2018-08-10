package com.neo.sk.webSpider.core

import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import org.slf4j.LoggerFactory
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.AskPattern._
import com.neo.sk.webSpider.Boot.{executor, scheduler, timeout}
import com.neo.sk.webSpider.core.IssueActor.{AddUndoArticleList, StartIssue}
import com.neo.sk.webSpider.core.ProxyActor.GetProxy
import com.neo.sk.webSpider.models.dao.{ArticleDao, IssueDao}
import com.neo.sk.webSpider.utils.{ChinaClient, EducationClient, HttpClientUtil}
import com.neo.sk.webSpider.Boot.{executor, proxyActor, scheduler, spiderManager, timeout}
import com.neo.sk.webSpider.core.VolumeActor.StartVolume

import scala.collection.mutable
import concurrent.duration._
import scala.concurrent.Future
/**
  * User: sky
  * Date: 2018/7/17
  * Time: 9:32
  */
object SpiderManager {
  val log = LoggerFactory.getLogger(this.getClass)

  trait Command
  final case class ChildDead(name:String,childRef:ActorRef[IssueActor.Command]) extends Command

  private final case object TimeOutKey

  case object TimeOut extends Command

  case object StartInit extends Command

  case class AddIssueList(list: List[(String,String)]) extends Command

  case class AddUndoIssueList(list:List[(String,String)]) extends Command

  case class ParseArticleList(cont:String) extends Command



  //  case class Get
  private val hash: mutable.Queue[(String, String)] = mutable.Queue()
  private var count1=0
  private var count2=0

  val behavior: Behavior[Command] = Behaviors.setup[Command] {ctx=>
    Behaviors.withTimers[Command] { implicit timer =>
      timer.startPeriodicTimer(TimeOutKey,TimeOut,5.minutes)
      idle
    }
  }

  private def idle(implicit timer: TimerScheduler[Command]): Behavior[Command] = {
    Behaviors.immutable[Command] { (ctx, msg) =>
      msg match {
        case StartInit=>
          val future:Future[Option[String]]=proxyActor ? (GetProxy(_))
          future.map{r=>
            HttpClientUtil.fetch("https://www.tandfonline.com/loi/mcsh20",r,None,None).map{
              case Right(t) =>
                val list=EducationClient.parseVolumeList(t)
                list.foreach{ r=>
                  getVolumeActor(ctx,r._2) ! StartVolume(r._1,r._2,r._3)
                }
              case Left(e) =>
                log.info("--- limit")

                timer.startSingleTimer(TimeOutKey,msg,30.seconds)
            }
          }
          Behaviors.same

        case TimeOut=>
          val a=hash.dequeue()
          getIssueActor(ctx,a._1,a._2) ! StartIssue(a._1)
//          getIssueActor(ctx,a._1,a._2) ! AddUndoArticleList
          Behaviors.same

        case msg: AddIssueList =>
          msg.list.foreach { r =>
            hash.enqueue(r)
          }
          count1=msg.list.size
          println(s"issue---count=$count1")
          val t=math.min(7,hash.size-1)
          for(i<-0 to t){
            val a=hash.dequeue()
            getIssueActor(ctx,a._1,a._2) ! StartIssue(a._1)
          }
          Behaviors.same

        case msg: AddUndoIssueList =>
          msg.list.foreach { r =>
            hash.enqueue(r)
          }
          count1=msg.list.size
          println(s"issue-undo--count=$count1")
          val t=math.min(7,hash.size-1)
          for(i<-0 to t) {
            val a = hash.dequeue()
            getIssueActor(ctx, a._1, a._2) ! AddUndoArticleList
          }
          Behaviors.same

        case msg:ChildDead=>
          count2+=1
          if(hash.nonEmpty){
            val a=hash.dequeue()
//            getIssueActor(ctx,a._1,a._2) ! AddUndoArticleList
            getIssueActor(ctx,a._1,a._2) ! StartIssue(a._1)
          }
          if(count1==count2){
            log.info("all fetch over")
            println("all fetch over")
          }
          Behaviors.same

        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }
  }

  private def getIssueActor(ctx: ActorContext[Command], id:String, issue:String) = {
    val childName = s"issueActor-${id.replace("/","-").replace("?","-").replace("=","-")}"
    log.info(childName)
    ctx.child(childName).getOrElse {
      val actor=ctx.spawn(IssueActor.init(id,issue), childName)
      ctx.watchWith(actor,ChildDead(childName,actor))
      actor
    }.upcast[IssueActor.Command]
  }

  private def getVolumeActor(ctx: ActorContext[Command], url:String) = {
    val childName = s"issueActor-${url.replace(" ","-").replace("/","-").replace("?","-").replace("=","-")}"
    log.info(childName)
    ctx.child(childName).getOrElse {
      ctx.spawn(VolumeActor.init(url), childName)
    }.upcast[VolumeActor.Command]
  }
}
