package com.neo.sk.webSpider

import akka.actor.{ActorSystem, Scheduler}
import akka.dispatch.MessageDispatcher
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.neo.sk.webSpider.service.HttpService

import scala.language.postfixOps
import scala.util.{Failure, Success}
import akka.actor.typed.scaladsl.adapter._
import com.neo.sk.webSpider.core._

/**
  * Created by Zhong on 2017/8/15.
  */
object Boot extends HttpService {

  import concurrent.duration._
  import com.neo.sk.webSpider.common.AppSettings._


  override implicit val system = ActorSystem("appSystem", config)
  // the executor should not be the default dispatcher.
  override implicit val executor: MessageDispatcher =
    system.dispatchers.lookup("akka.actor.my-blocking-dispatcher")

  override implicit val materializer = ActorMaterializer()

  override implicit val timeout = Timeout(20 seconds) // for actor asks

  override implicit val scheduler = system.scheduler

  val log: LoggingAdapter = Logging(system, getClass)

  val spiderManager=system.spawn(SpiderManager.behavior,"spiderManager")

  def main(args: Array[String]) {
    log.info("Starting.")
    val binding = Http().bindAndHandle(routes, httpInterface, httpPort)
    binding.onComplete {
      case Success(b) ⇒
        val localAddress = b.localAddress
        println(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
      case Failure(e) ⇒
        println(s"Binding failed with ${e.getMessage}")
        system.terminate()
        System.exit(-1)
    }
  }
}
