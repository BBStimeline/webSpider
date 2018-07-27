package com.neo.sk.webSpider.utils

import java.io.File

import com.neo.sk.webSpider.Boot.executor
import com.neo.sk.webSpider.common.AppSettings
import com.neo.sk.webSpider.shared.ptcl.ComRsp
import org.slf4j.LoggerFactory
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.Future
/**
  * Created by Zhong on 2017/7/18.
  */
object HestiaClient extends HttpUtil with CirceSupport {
  private val log = LoggerFactory.getLogger("com.neo.sk.util.HestiaClient")

//  private val hestiaBaseUrl = AppSettings.hestiaProtocol + "://" + AppSettings.hestiaHost + ":" + AppSettings.hestiaPort
  private val hestiaBaseUrl = AppSettings.hestiaProtocol + "://" + AppSettings.hestiaDomain

  final case class UploadImgToHestiaReq(url: String)

  final case class UploadImgToHestiaRsp(fileName: String, errCode: Int, msg: String)

  private def getHestiaUrl = AppSettings.hestiaProtocol + "://" + AppSettings.hestiaHost + ":" + AppSettings.hestiaPort + "/hestia/files/uploadByUrl"

  //https
  def genImgUrl(fileName: String) =
    AppSettings.hestiaImgProtocol + "://" + AppSettings.hestiaDomain + "/hestia/files/image/" + AppSettings.hestiaAppId + s"/$fileName"

  def uploadImgByUrl(imgUrl: String): Future[Option[String]] = {
    val url = hestiaBaseUrl + "/hestia/files/uploadByUrl"
    val methodName = s"uploadImgByUrl $url"
    val parameters = List(
      "appId" -> AppSettings.hestiaAppId,
      "sn" -> System.nanoTime().toString
    )

    val (t, n, s) = SecureUtil.generateSignatureParameters(parameters.map(_._2), AppSettings.hestiaSecureKey)

    val parametersAll = parameters ::: List("timestamp" -> t, "nonce" -> n, "signature" -> s)

    postJsonRequestSend(methodName, url, parametersAll, UploadImgToHestiaReq(imgUrl).asJson.noSpaces).map {
      case Right(str) =>
        decode[ComRsp](str) match {
          case Right(common) =>
            if(common.errCode == 0) {
              decode[UploadImgToHestiaRsp](str) match {
                case Right(rsp) =>
                  Some(genImgUrl(rsp.fileName))
    
                case Left(e) =>
                  log.info(s"$methodName decode error: $e")
                  None
              }
            } else {
              log.error(s"$methodName get error response : ${common.errCode}, ${common.msg}")
              None
            }
          case Left(e) =>
            log.info(s"$methodName common decode error: $e")
            None
        }

        
      case Left(e) =>
        log.info(s"$methodName postJsonRequestSend error: $e")
        None
    }
  }

  def upload(file: File, fileName: String): Future[Either[String, String]] = {
    val uploadUrl = hestiaBaseUrl + "/hestia/files/upload"
    val sn = AppSettings.hestiaAppId + System.nanoTime().toString
    val (timestamp, nonce, signature) = SecureUtil.generateSignatureParameters(List(AppSettings.hestiaAppId, sn), AppSettings.hestiaSecureKey)
    postFileRequestSend(s"upload ${file.getName}", uploadUrl,
      List(
        "appId" -> AppSettings.hestiaAppId,
        "sn" -> sn,
        "timestamp" -> timestamp,
        "nonce" -> nonce,
        "signature" -> signature
      ), file, fileName).map {
      case Right(str) =>
        decode[UploadImgToHestiaRsp](str) match {
          case Right(rsp) =>
            if (rsp.errCode == 0)
              Right(rsp.fileName)
            else {
              log.error(s"upload ${file.getName}  error.error:${rsp.msg}")
              Left(s"${rsp.msg}")
            }

          case Left(e) =>
            log.error(s"upload ${file.getName}  parse error.$e")
            Left(s"Error.$e")
        }

      case Left(e) =>
        log.error(s"upload ${file.getName} failed:" + e)
        Left(s"Error.$e")
    }
  }

  def main1(args: Array[String]): Unit = {
    val f = uploadImgByUrl("http://att.newsmth.net/nForum/att/FamilyLife/1759988330/10046")
    f.map{
      s =>
        print(s)
    }.failed.foreach{ e =>
      print("wrong")
    }
  }
}
