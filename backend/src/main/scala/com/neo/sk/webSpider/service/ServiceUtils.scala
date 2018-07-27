package com.neo.sk.webSpider.service

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import com.neo.sk.webSpider.common.AppSettings
import com.neo.sk.webSpider.protocols.CommonProtocol._
import com.neo.sk.webSpider.utils.SecureUtil.PostEnvelope
import com.neo.sk.webSpider.utils.{CirceSupport, SecureUtil}
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.Error
import io.circe.Decoder
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * User: Taoz
  * Date: 11/18/2016
  * Time: 7:57 PM
  */
object ServiceUtils {
  private val log = LoggerFactory.getLogger("com.neo.sk.smallspider.service.ServiceUtils")
  private val authCheck = AppSettings.authCheck
}

trait ServiceUtils extends CirceSupport {

  import ServiceUtils._

  final val INTERNAL_ERROR = CommonRsp(10001, "Internal error.")

  final val JsonParseError = CommonRsp(10002, "Json parse error.")

  def htmlResponse(html: String): HttpResponse = {
    HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, html))
  }

  def jsonResponse(json: String): HttpResponse = {
    HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, json))
  }

  def dealFutureResult(future: ⇒ Future[server.Route]): server.Route = onComplete(future) {
    case Success(route) =>
      route
    case Failure(e) =>
      e.printStackTrace()
      complete(INTERNAL_ERROR)
  }

  def ensureAuth(
                  appClientId: String,
                  timestamp: String,
                  nonce: String,
                  sn: String,
                  data: List[String],
                  signature: String
                )(f: => Future[server.Route]): server.Route = {
    val p = getSecureKey(appClientId) match {
      case Some(secureKey) =>
        val paramList = List(appClientId.toString, timestamp, nonce, sn) ::: data
        if (timestamp.toLong + 120000 < System.currentTimeMillis()) {
          Future.successful(complete(RequestTimeout))
        } else if (SecureUtil.checkSignature(paramList, signature, secureKey)) {
          f
        } else {
          Future.successful(complete(SignatureError))
        }
      case None =>
        Future.successful(complete(AppClientIdError))
    }
    dealFutureResult(p)
  }

  def ensurePostEnvelope(e: PostEnvelope)(f: => Future[server.Route]) = {
    ensureAuth(e.appId, e.timestamp, e.nonce, e.sn, List(e.data), e.signature)(f)
  }

  private def getSecureKey(appId: String) = AppSettings.appSecureMap.get(appId)

  def dealPostReq[A](f: A => Future[server.Route])(implicit decoder: Decoder[A]): server.Route = {
    entity(as[Either[Error, PostEnvelope]]) {
      case Right(envelope) =>
        if(authCheck) {
          ensurePostEnvelope(envelope) {
            decode[A](envelope.data) match {
              case Right(req) =>
                f(req)
      
              case Left(e) =>
                log.error(s"json parse detail type error: $e")
                Future.successful(complete(JsonParseError))
            }
          }
        } else {
          dealFutureResult {
            decode[A](envelope.data) match {
              case Right(req) =>
                f(req)
    
              case Left(e) =>
                log.error(s"json parse detail type error: $e")
                Future.successful(complete(JsonParseError))
            }
          }
        }

      case Left(e) =>
        log.error(s"json parse PostEnvelope error: $e")
        complete(JsonParseError)
    }
  }



}
