package com.neo.sk.webSpider.shared

/**
  * User: Taoz
  * Date: 5/30/2017
  * Time: 10:37 AM
  */
/**
  *
  * Created by liuziwei on 2017/5/5.
  *
  */


package object ptcl {

  trait CommonRsp {
    val errCode: Int
    val msg: String
  }

  final case class ErrorRsp(
    errCode: Int,
    msg: String
  ) extends CommonRsp

  final case class SuccessRsp(
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  final case class ComRsp(
    errCode: Int,
    msg: String
  ) extends CommonRsp

  final val JsonParseError = ErrorRsp(11001, "json parse error.")

  //管理员登陆
  case class AdminConfirm(
    adminName: String,
    passWord: String
  )

  final val SignatureError = ErrorRsp(12001, "signature error.")

  final val NoAdminError = ErrorRsp(12002, "no admin error.")

  //版面列表页
  case class BoardsListReq(
    boardType: Int, // -1:废弃板块  0普通板块 1：所有板块 2: 热门板块
    page: Int,
    count: Int = 10
  )

  case class BoardsPageRsp(
    boardsLs: List[BoardInfo],
    totalNum: Int,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  case class BoardInfo(
    id: Long,
    boardNameEn: String,
    boardNameCn: String,
    moderator: String,
    todayNew: Int,
  )

  case class BoardsNumRsp(
    hot: Int,
    normal: Int,
    abandon: Int,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  case class BoardInfoReq(id: Long, boardName: String)

  case class BoardStatistic(
    updateTime: Long,
    averageDelay: Double,
    todayPosts: Int
  )

  case class BoardInfoRsp(
    boardStatistic: BoardStatistic,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  case class HistoryPostsReq(
    date: String,
    boardName: String
  )

  case class HistoryPosts(totalPosts: Int, mainPosts: Int, replyPosts: Int)

  case class HistoryPostsRsp(
    info: HistoryPosts,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  //帖子信息页
  case class PostsReq(postType: Int, interval: Int) // 0-参与人数最多，1-回复者最多

  case class Post(
    postId: Long,
    boardName: String,
    title: String,
    author: String,
    participants: Int
  )

  case class PostRsp(
    posts: List[Post],
    errCode: Int = 0,
    msg: String = "ok") extends CommonRsp

  case class EditPostNum(num: Int)

  case class EditPostNumRsp(
    num: EditPostNum,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  case class MaxPostsBoardsReq(interval: Int)

  case class MaxPostsBoard(
    boardName: String,
    boardNameCn: String,
    postNum: Int)

  case class MaxPostsBoardsRsp(
    boards: List[MaxPostsBoard],
    errCode: Int = 0,
    msg: String = "ok"
  )

  case class GetDelayReq(boardName: String, timeLimit: String)

  case class Delay(delay: Double)

  case class GetDelayRsp(
    averageDelay: Delay,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp


  /*监控页面*/

  //添加版面
  case class InsertBoardReq(
    boardName: String,
    boardNameCn: String,
    isHot: Int,
    needLogin: Int
  )

  //阿里云代理配置
  case class ProxyConfig(
    addGate: Option[Int],
    removeGate: Option[Int],
    isAvailable: Option[Boolean]
  )
  
  case class MaxTaskSetting(
    newNumber: Option[Int],
    backupLimiter: Option[Int]
  )

  case class ProxyConfigInfo(
    addGate: Int,
    removeGate: Int,
    backupProxyMode: Boolean,
    backupProxyAvailable: Boolean
  )
  
  case class MaxTaskNumberInfo(
    maxNumber: Int,
    backupLimiter: Int
  )

  case class ProxyConfigInfoRsp(
    info: ProxyConfigInfo,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp
  
  case class MaxTaskNumberInfoRsp(
    info: MaxTaskNumberInfo,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

}
