package com.neo.sk.webSpider.service

import akka.actor.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by Zhong on 2017/8/15.
  */
trait HttpService extends ApiService
  with ResourceService {

  implicit val system: ActorSystem

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  implicit val scheduler: Scheduler


  val routes: Route =
    pathPrefix("webSpider") {
      resourceRoutes ~ apiRoutes
    }


}

