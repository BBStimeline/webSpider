package com.neo.sk.webSpider.front

/**
  * Created by TangYaruo on 2017/11/2.
  */
object Routes {

  private val baseUrl = "/webSpider/front"

  val adminHome: String = baseUrl + "/adminHome"
  val adminLogin: String = baseUrl + "/adminLogin"
  val loginSubmit: String = baseUrl + "/loginSubmit"

  /*版面信息页*/
  val boardInfo: String = baseUrl + "/boardInfo"
  val getBoardsPage: String = baseUrl + "/getBoardsPage"
  val getBoardsNum: String = baseUrl + "/getBoardsNum"
  val getBoardInfo: String = baseUrl + "/getBoardInfo"
  val getHistoryPosts: String = baseUrl + "/getHistoryPosts"
  val getDelayByTime: String = baseUrl + "/getDelayByTime"

  /*监控页面*/
  val supervise : String =  baseUrl + "/supervise"
  val checkProxyConfig: String = baseUrl + "/checkProxyConfig"
  val editProxyConfig: String = baseUrl + "/editProxyConfig"
  val addNewBoard: String = baseUrl + "/addNewBoard"
  val checkMaxTaskNumber: String = baseUrl + "/checkMaxTaskNumber"
  val changeMaxTaskNumber: String = baseUrl + "/changeMaxTaskNumber"

  /*帖子信息页*/
  val postsInfo: String = baseUrl + "/postsInfo"
  val searchPost: String = baseUrl + "/searchPost"
  val searchMaxPostsBoards: String = baseUrl + "/searchMaxPostsBoards"


  object AdminService {

    private val baseUrl = "/webSpider/admin/spider"

    val start: String = baseUrl + "/start"
    val stop: String = baseUrl + "/stop"

  }


}
