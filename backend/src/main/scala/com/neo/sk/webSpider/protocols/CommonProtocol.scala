package com.neo.sk.webSpider.protocols

import com.neo.sk.webSpider.utils.SecureUtil

/**
  * Created by Zhong on 2017/8/17.
  */
object CommonProtocol {

  trait Request

  case class Plus(value: Int) extends Request

  case class Minus(value: Int) extends Request


  trait Response {
    val errCode: Int
    val msg: String
  }

  object EventType {
    val AddEvent = "add"
    val EditEvent = "edit"
    val DeleteEvent = "delete"
    val RecoverEvent = "recover"
    val PictureEvent = "updateHestiaImg"
  }

  case class CommonRsp(errCode: Int = 0, msg: String = "ok") extends Response

  val SuccessRsp = CommonRsp()

  val SignatureError = CommonRsp(1000001, "signature error.")

  val RequestTimeout = CommonRsp(1000003, "request timestamp is too old.")

  val AppClientIdError = CommonRsp(1000002, "appClientId error.")


  /*Admin*/
  case class RefreshReq() extends Request

  case class GetStopBoardReq() extends Request

  /**
    * Api
    *
    */

  type PostEnvelope = SecureUtil.PostEnvelope

  //查询用户帖子请求
  case class UserReq(userId: String,
    onlyTopic: Int,
    page: Int,
    pageSize: Int) extends Request

  //查询用户帖子结果
  case class PostsContentListOfUserRsp(
    author: Author,
    posts: List[Post],
    //                                               pagination: Pagination,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  //按版面名称、帖子Id查找帖子
  case class PostSearchReq(posts: List[BoardNameAndPostId], contentType: Option[String] = Some("text")) extends Request

  //查询主贴及其跟帖
  case class PostSearchByTopicIdReq(
    boardName: String,
    topicId: Long,
    page: Option[Int],
    pageSize: Option[Int],
    contentType: Option[String] = Some("text")) extends Request

  //帖子查询结果
  case class PostSearchRsp(
    posts: List[Post],
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  //帖子查询结果 for Galaxy
  case class PostSearchRsp4Galaxy(
    posts: List[Post4Galaxy],
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  //tiny帖子查询结果
  case class TinyPostSearchRsp(
    posts: List[TinyPost],
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  //板块帖子列表回复 for 水木
  case class BoardPageRsp(
    onlineNum: Int, //在线人数
    postNum: Int, //今日发帖数
    boardMaster: String, //版主
    topPost: Option[List[PostForSmth]], //置顶帖
    normalPost: List[PostForSmth], //普通帖子
    page: Int = 0, //页数
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  case class PostDetailRsp(
    pageNum: Int, //总页数
    mainFloor: Option[MainFloor], //主贴
    normalFloor: List[NormalFloor], //回帖
    page: Int = 0, //当前页数
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response
  
  case class PostEventRsp(
    events: List[PostEvent],
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  case class LikesRsp(
    likes: List[Like],
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response
	
	case class TopicInfoRsp(
		info: List[TopicGeneralInfo],
		errCode: Int = 0,
		msg: String = "ok"
	) extends Response

  //请求大于[id]的[count]个帖子记录
  case class PostsReq(id: Long, count: Int, contentType: Option[String] = Some("text")) extends Request

  //请求大于[id]的[count]个帖子记录 for Galaxy
  case class PostsReq4Galaxy(id: Long, count: Int) extends Request
  
  //请求大于[id]的[count]个帖子事件记录 for Galaxy
  case class PostEventReq(id: Long, count: Int) extends Request

  //请求大于[id]的[count]个like记录
  case class LikesReq(id: Long, count: Int) extends Request

  //请求大于[id]的[count]个图片记录
  case class ImgsReq(id: Long, count: Int, contentType: Option[String] = Some("text")) extends Request

  //请求大于[id]的[count]个十大记录
  case class TopTensReq(id: Long, count: Int, contentType: Option[String] = Some("text")) extends Request

  //请求大于[id]的[count]个用户记录
  case class UsersReq(id: Long, count: Int, contentType: Option[String] = Some("text")) extends Request

  //获取新帖
  case class NewPostsReq(page: Int, count: Int, contentType: Option[String] = Some("text")) extends Request

  //获取招聘新帖
  case class JobPostsReq(page: Int, count: Int, contentType: Option[String] = Some("text")) extends Request

  case class TopTenReq() extends Request
	
	case class RecentReplyReq(lastTime: Long) extends Request

  //十大结果
  case class TopTenRsp(
    posts: List[TopTen],
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  //查询图片请求
  case class ImageUrlReq(
    boardName: String,
    postId: Long,
    imgId: Long
  ) extends Request

  //图片地址结果
  case class ImageUrlRsp(
    imgUrl: String,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  //按用户、时间查询帖子请求
  case class UsersPostsReq(
    userId: List[String],
    startTime: Long,
    contentType: Option[String] = Some("text")
  ) extends Request

  //按板块查询帖子请求
  case class BoardPostsReq(
    boardName: String,
    postId: Option[Long],
    length: Option[Int],
    contentType: Option[String] = Some("text")
  ) extends Request

  //多板块查询帖子请求
  case class MultiBoardPostsReq(
    boardList: List[String],
    postBoard: Option[String],
    postId: Option[Long],
    length: Option[Int],
    contentType: Option[String] = Some("text")
  ) extends Request

  //刷新帖子请求
  case class RefreshPostReq(
    boardName: String,
    postId: Long
  ) extends Request

  //查询用户信息
  case class SearchUserReq(userId: String) extends Request

  //板块帖子列表 for 水木
  case class PostListReq(
    boardName: String,
    page: Option[Int],
    length: Option[Int],
    contentType: Option[String] = Some("text")
  ) extends Request

  //帖子详情 for 水木
  case class PostDetailReq(
    boardName: String,
    postId: Long,
    length: Option[Int],
    page: Option[Int],
    contentType: Option[String] = Some("text")
  ) extends Request

  case class SearchUserRsp(user: Author,
    errCode: Int = 0,
    msg: String = "ok") extends Response

  case class Post(
    id: Long, //唯一标识
    boardName: String, //板块名称
    topicId: Long, //主贴话题id
    postId: Long, //帖子id
    quoteId: Long, //引文的帖子id
    title: String, //标题
    authorId: String, //作者id
    postTime: String, //发帖时间
    timestamp: Long, //发帖时间戳
    content: String, //正文
    quoteAuthor: String, //引文作者id
    quoteTitle: String, //引文标题
    quote: String, //引文内容
    ip: String, //ip
    img: List[String], //图片列表
    mainPost: Boolean,
    boardNameCn: String //板块中文名称
  )

  case class TinyPost(
    id: Long, //唯一标识
    boardName: String, //板块名称
    topicId: Long, //主贴话题id
    postId: Long, //帖子id
    quoteId: Long, //引文的帖子id
    title: String, //标题
    authorId: String, //作者id
    timestamp: Long, //发帖时间戳
    quoteAuthor: String, //引文作者id
    quoteTitle: String, //引文标题
    quote: String, //引文内容
    ip: String, //ip
    img: List[String], //图片列表
    mainPost: Boolean,
    boardNameCn: String //板块中文名称
  )


  case class Post4Galaxy(
    id: Long, //唯一标识
    boardName: String, //板块名称
    topicId: Long, //主贴话题id
    postId: Long, //帖子id
    quoteId: Long, //引文的帖子id
    url: String, //帖子url
    title: String, //标题
    authorId: String, //作者id
    nickname: String, //作者昵称
    timestamp: Long, //发帖时间戳
    contentText: String, //正文text
    contentHtml: String, //正文html
    quoteAuthor: String, //引文作者id
    quoteTitle: String, //引文标题
    quote: String, //引文内容
    ip: String, //ip
    imgs: String, //图片列表
    hestiaImgs: String, //图片服务器url
    mainPost: Boolean,
    boardNameCn: String //板块中文名称
  )

  case class Pagination(
    itemAllCount: Int,
    itemPageCount: Int,
    pageAllCount: Int, //总页数
    pageCurrentCount: Int //当前页数
  )

  case class Author(
    userId: String,
    userName: String,
    userImage: String,
    gender: Char
  )

  case class BoardNameAndPostId(boardName: String, postId: Long)

  case class TopTen(
    index: Int,
    boardName: String,
    postId: Long,
    topic: String,
    hotRank: Long,
    postTime: Long = System.currentTimeMillis(),
    userId: String = "user",
    boardNameCn: String = "board",
    content: String = ""
  )

  //水木接口使用的格式数据
  case class PostForSmth(
    tittle: String, //标题
    tittleUrl: String, //url 板块+id 如/nForum/article/AutoWorld/1941412285
    postTime: String, //发帖时间
    author: String, //作者id
    grade: Option[Int], //评分 可选
    like: Option[Int], //like数 可选
    replyNum: Int, //回帖数
    lastReply: String, //最新回复时间
    lastReplyAuthor: String, //最新回复作者id
    isAttach: Boolean = false //可无
  )

  case class MainFloor(
    author: AuthorInfo, //作者信息
    topicId: Long,
    postId: Long,
    tittle: String, //标题
    station: String, //发帖时间
    content: String, //正文
    origin: String, //来源
    likeList: Option[List[LikeItem]], //like列表
    imgList: List[String] = Nil //图片列表
  )

  case class NormalFloor(
    author: AuthorInfo = AuthorInfo(), //作者信息
    topicId: Long,
    postId: Long,
    floor: String = "", //楼层数
    station: String = "", //回帖时间
    tittle: String = "", //标题
    origin: String = "", //来源
    content: String = "", //正文
    parent: Option[String] = None, //引文
    replyNum: Int = 0, //引文id
    imgList: List[String] = Nil //图片列表
  )

  case class LikeItem(
    likeScore: Option[Int], //评分
    likeUser: String, //like的用户id
    likeMsg: String, //like的配文
    likeTime: String //like时间
  )

  case class AuthorInfo(
    uid: String = "", //id
    sex: String = "", //性别
    headImg: String = "", //头像
    nickName: Option[String] = None, //昵称
    baseInfo: List[(String, String)] = Nil //等级之类的可有可无
  )
  
  case class PostEvent(
    eventId:Long,
    eventAction:String,//add,edit,delete
    eventTime:Long,
    eventData:String
  )
  
  //这个post表获取字段也是这些
  case class AddPostData(
    appSystemId:Long,//post的表id
    topicId:Long,
    postId:Long,
    isMain:Boolean,
    title:String,
    authorId:String,
    contentHtml:String,
    contentText:String,
    hestiaImgs:String,
    imgs:String,
    postTime:Long,
    boardName:String,
    boardNameCn:String,
    url:Option[String],
    ip:Option[String],
    quoteId:Option[Long],
    quoteAuthor:Option[String],
    quoteTitle:Option[String],
    quoteContent:Option[String],
    nickname:String
  )
  
  case class EditPostData(
    topicId:Long,
    postId:Long,
    boardName:String,
    contentHtml:String,
    contentText:String,
    hestiaImgs:String,
    imgs:String,
    title:String,
    editTime:Long
  )
  
  
  case class DeleteData(
    topicId:Long,
    postId:Long,
    boardName:String,
    deleteTime:Long
  )
  
  case class UpdateHestiaImgData(
    topicId:Long,
    postId:Long,
    boardName:String,
    hestiaImgs:String
  )
  
  case class Like(
    id: Long,
    score: Int,
    authorId: String,
    content: String,
    timestamp: Long,
    boardName: String,
    postId: Long
  )
  
  case class TopicGeneralInfo(
		boardName: String,
		postId: Long,
		replyNum: Int
	)

}
