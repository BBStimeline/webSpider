package com.neo.sk.webSpider.common

import java.util.concurrent.TimeUnit

import com.neo.sk.webSpider.utils.SessionSupport.SessionConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

/**
  * User: Taoz
  * Date: 9/4/2015
  * Time: 4:29 PM
  */
object AppSettings {

  import collection.JavaConverters._

  private implicit class RichConfig(config: Config) {
    val noneValue = "none"

    def getOptionalString(path: String): Option[String] =
      if (config.getAnyRef(path) == noneValue) None
      else Some(config.getString(path))

    def getOptionalLong(path: String): Option[Long] =
      if (config.getAnyRef(path) == noneValue) None
      else Some(config.getLong(path))

    def getOptionalDurationSeconds(path: String): Option[Long] =
      if (config.getAnyRef(path) == noneValue) None
      else Some(config.getDuration(path, TimeUnit.SECONDS))
  }


  val log = LoggerFactory.getLogger(this.getClass)
  val config = ConfigFactory.parseResources("product.conf").withFallback(ConfigFactory.load())

  val appConfig = config.getConfig("app")

  val httpInterface = appConfig.getString("http.interface")
  val httpPort = appConfig.getInt("http.port")

  val serverProtocol = appConfig.getString("server.protocol")
  val serverHost = appConfig.getString("server.host")


  val slickConfig = config.getConfig("slick.db")
  val slickUrl = slickConfig.getString("url")
  val slickUser = slickConfig.getString("user")
  val slickPassword = slickConfig.getString("password")
  val slickMaximumPoolSize = slickConfig.getInt("maximumPoolSize")
  val slickConnectTimeout = slickConfig.getInt("connectTimeout")
  val slickIdleTimeout = slickConfig.getInt("idleTimeout")
  val slickMaxLifetime = slickConfig.getInt("maxLifetime")

  val postsConfig = appConfig.getConfig("posts")
  val postsListMinLength = postsConfig.getInt("postsListMinLength")
  val postsListMaxLength = postsConfig.getInt("postsListMaxLength")

  val dependenceConfig = config.getConfig("dependence")

  val spiderConfig = dependenceConfig.getConfig("spider.config")
	val authCheck = spiderConfig.getBoolean("authCheck")
  val intervalMaxTaskNumber = spiderConfig.getInt("intervalMaxTaskNumber")
  val backupLimiter = spiderConfig.getInt("backupLimiter")
	val hottestBoardInterval = spiderConfig.getInt("hottestBoardInterval")
  val hotBoardInterval = spiderConfig.getInt("hotBoardInterval")
  val newPostInterval = spiderConfig.getInt("newPostInterval")
  val historyPostInterval = spiderConfig.getInt("historyPostInterval")
  val useProxy = spiderConfig.getBoolean("useProxy")
  val spiderNumber = spiderConfig.getInt("spiderNumber")
  val spiderIdleInterval = spiderConfig.getInt("spiderIdleInterval")
  val newTopTenInterval = spiderConfig.getInt("newTopTenInterval")
  val destImageFilePath = spiderConfig.getString("destImageFilePath")
  val daytimeUpdateCheckInterval = spiderConfig.getInt("daytimeUpdateCheckInterval")
  val nighttimeUpdateCheckInterval = spiderConfig.getInt("nighttimeUpdateCheckInterval")
  val imageRefreshInterval = spiderConfig.getInt("imageRefreshInterval")

  val smthConfig = spiderConfig.getConfig("smth.account")
  val smthUserId = smthConfig.getStringList("userId").asScala
  val smthUserPwd = smthConfig.getStringList("userPwd").asScala

  require(smthUserId.length == smthUserPwd.length)

  val byrConfig = spiderConfig.getConfig("byr.account")
  val byrUserId = byrConfig.getStringList("userId").asScala
  val byrUserPwd = byrConfig.getStringList("userPwd").asScala

  require(byrUserId.length == byrUserPwd.length)
  
  val backupProxyConfig = spiderConfig.getConfig("backupProxy")
  val backupProxyIp = backupProxyConfig.getStringList("ip").asScala
  val backupProxyPort = backupProxyConfig.getIntList("port").asScala
  val backupProxyUsername = backupProxyConfig.getStringList("username").asScala
  val backupProxyPassword = backupProxyConfig.getStringList("password").asScala
  val backupProxyAddGate = backupProxyConfig.getInt("addGate")
  val backupProxyRemoveGate = backupProxyConfig.getInt("removeGate")
  val backupProxyDefaultAvailable = backupProxyConfig.getBoolean("defaultAvailable")
  
  require(backupProxyIp.length == backupProxyPort.length &&
    backupProxyIp.length == backupProxyUsername.length &&
    backupProxyIp.length == backupProxyPassword.length)

  val hestiaConfig = dependenceConfig.getConfig("hesita.config")
  val hestiaAppId = hestiaConfig.getString("appId")
  val hestiaSecureKey = hestiaConfig.getString("secureKey")
  val hestiaProtocol = hestiaConfig.getString("protocol")
  val hestiaImgProtocol = hestiaConfig.getString("imgProtocol")
  val hestiaHost = hestiaConfig.getString("host")
  val hestiaDomain = hestiaConfig.getString("domain")
  val hestiaPort = hestiaConfig.getInt("port")


  val tmpDir = System.getProperty("java.io.tmpdir")

  val mailConfig = config.getConfig("mail.conf")
  val EMAIL_ADDRESS = mailConfig.getString("EMAIL_ADDRESS")
  val EMAIL_PASSWORD = mailConfig.getString("EMAIL_PASSWORD")
  val SMTPHOST = mailConfig.getString("SMTPHOST")
  val SMTPPORT = mailConfig.getString("SMTPPORT")
  val IMAP_SERVER = mailConfig.getString("IMAP_SERVER")
  val IMAP_PROTOCOL = mailConfig.getString("IMAP_PROTOCOL")

  val sessionConfig = {
    val sConf = config.getConfig("session")
    SessionConfig(
      cookieName = sConf.getString("cookie.name"),
      serverSecret = sConf.getString("serverSecret"),
      domain = sConf.getOptionalString("cookie.domain"),
      path = sConf.getOptionalString("cookie.path"),
      secure = sConf.getBoolean("cookie.secure"),
      httpOnly = sConf.getBoolean("cookie.httpOnly"),
      maxAge = sConf.getOptionalDurationSeconds("cookie.maxAge"),
      sessionEncryptData = sConf.getBoolean("encryptData")
    )


  }

  val appSecureMap = {
    import collection.JavaConverters._
    val appIds = appConfig.getStringList("client.appIds").asScala
    val secureKeys = appConfig.getStringList("client.secureKeys").asScala
    require(appIds.length == secureKeys.length, "appIdList.length and secureKeys.length not equel.")
    appIds.zip(secureKeys).toMap
  }


}
