package com.neo.sk.webSpider.core

import akka.actor.{ActorSystem, Props}
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.util.Timeout
import akka.actor.Scheduler
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration
import com.neo.sk.webSpider.Boot

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.neo.sk.webSpider
import akka.util.Timeout
import com.neo.sk.webSpider.common.AppSettings
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.{Failure, Random, Success}
import scala.concurrent.duration._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._

import scala.collection.mutable
import scala.concurrent.duration._
import com.neo.sk.webSpider.utils.{HttpClientUtil, HttpUtil}

/**
  * User: sky
  * Date: 2017/12/11
  * Time: 15:20
  */
object ProxyActor extends HttpUtil {
  private val log = LoggerFactory.getLogger(this.getClass)
  case class ProxyRes(
                       code:Int,
                       data:ProxyData
                     )
  case class ProxyData(
                        count:Int,
                        proxy_list:List[String]
                      )

  case class ProxyNewRes(
                        code:Int,
                        msg:List[ProxyNewData]
                        )

  case class ProxyNewData(
                         port:Int,
                         ip:String
                         )
  case class GetProxyRes(data:Option[(String,Int)])
  trait Msg
  private case object TimerKey
  private case object TimerOnceKey
  private case object DelTimerKey
  private case object Timeout extends Msg
  private case object DelTimeout extends Msg
  private case object LoadProxy extends Msg
  private case object TestProxy extends Msg
  case class GetProxy(peer:ActorRef[Option[String]]) extends Msg
  case class GetUserProxy(num:Int,peer:ActorRef[GetProxyRes]) extends Msg
  case class GetAndTestProxy(num:Int,peer:ActorRef[GetProxyRes]) extends Msg
  case class SetProxyPeriod(mint:Int) extends Msg
  private val delOutTime:Long=5*60*1000
  private val proxy=new mutable.HashMap[(String, Int), (Double,Long)]()
  private var proxyList:List[String] =Nil
//  private val alreadyProxy :List[(String,Int,String,String)]= AppSettings.proxyList
//  proxy.put(("47.93.119.6",30332),(5.0,System.currentTimeMillis()))

  def behavior: Behavior[Msg] ={
    Behaviors.withTimers(timers => prepareBehavior(timers))
  }

  private def prepareBehavior(timers: TimerScheduler[Msg])={
    log.info("ProxyActor start working")
    timers.startSingleTimer(TimerOnceKey,Timeout,1.seconds)
    timers.startPeriodicTimer(TimerKey,Timeout,100.seconds)
    timers.startPeriodicTimer(DelTimerKey,DelTimeout,5.minutes)
    active(timers)
  }

  private def active( timers: TimerScheduler[Msg]): Behavior[Msg] = {
    Behaviors.immutable[Msg] { (ctx, msg) =>
      msg match {
        case Timeout =>
          getNewProxy.map{ps=>
//            println(proxyList.size)
            val now=System.currentTimeMillis()
            ps.getOrElse(Nil).map{p=>
              proxy.get(p) match {
                case Some(t)=>
                case None=>
                  proxy.put(p,(1.0,now))
              }
            }
            if(ps.getOrElse(Nil).isEmpty){
              timers.startSingleTimer(TimerOnceKey,Timeout,5.seconds)
            }
            ctx.self ! TestProxy
            ctx.self ! LoadProxy
          }
          Behaviors.same

        case TestProxy=>
          proxy.filter(r=>r._2._1==1.0||r._2._1==0.5).take(10).map{r=>
            testProxy(r._1._1,r._1._2).map { t =>
              proxy.update(r._1, (t, r._2._2))
              if(t==5.0){
                proxyList::=(r._1._1+":"+r._1._2)
                log.info(s"find an useful proxy ${r._1} and the useful num=${proxyList.size}")
              }
            }
          }
          Behaviors.same

        case DelTimeout=>
          val now=System.currentTimeMillis()-delOutTime
          proxy.filter(_._2._2<now).map(r=>
            proxy.remove(r._1)
          )
          ctx.self ! LoadProxy
          Behaviors.same

        case LoadProxy=>
          proxyList=proxy.filter(_._2._1==5.0).toList.sortBy(_._2._2).reverse.take(15).map(x=> x._1._1 + ":" + x._1._2)
          Behaviors.same


          //获取普通代理
        case GetProxy(peer) =>
          if (proxyList.nonEmpty) {
            val pxy = proxyList((new Random).nextInt(proxyList.size-1))
            peer ! Some(pxy)
          } else {
            peer ! None
          }
          Behaviors.same

      }
  }
  }

  def testProxy(ip:String,port:Int)={
    val url="https://www.baidu.com/"
    getRequestWithProxy("testProxy",url,Nil,ip,port,"utf-8").map{
      case Right(value)=>
        if(value._1==200) 5.0 else if(value._1==503||value._1==500) 0.5 else 0
      case Left(e)=>
        log.debug(s"test proxy——${ip+":"+port} error.$e")
        0
    }
  }

  def getNewProxy = {
    val url = "http://piping.mogumiao.com/proxy/api/get_ip_bs?appKey=" +
      "af351620cd15489a8ef57e7207ba6962&count=10&expiryDate=1&format=1"
    HttpClientUtil.fetch(url,None,None,None).map {
      case Right(value) =>
        decode[ProxyNewRes](value) match {
          case Right(res) =>
            if (res.code == 0) {
              Some(res.msg.map(r=>
                (r.ip,r.port))
              )
            } else {
              log.debug(s"get proxy error. code=${res.code}")
              None
            }

          case Left(e) =>
            log.debug(s"decode get proxy error.$e")
            None
        }
      case Left(e) =>
        log.debug(s"getRequestSend get proxy error.$e")
        None
    }
  }


  def randomList(n:Int,length:Int)={
    var arr= 0 to (if(length>0) length else 0) toArray
    var outList:List[Int]=Nil
    var border=arr.length//随机数范围
    for(i<-0 until n){//生成n个数
      if(border>0){
        val index=(new Random).nextInt(border)
        outList=outList:::List(arr(index))
        arr(index)=arr.last//将最后一个元素换到刚取走的位置
        arr=arr.dropRight(1)//去除最后一个元素
        border-=1
      }
    }
    outList.head
  }

}
