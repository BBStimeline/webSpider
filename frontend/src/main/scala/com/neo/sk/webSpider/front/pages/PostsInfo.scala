package com.neo.sk.webSpider.front.pages

import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.Http
import com.neo.sk.webSpider.front.utils.MyUtil._
import com.neo.sk.webSpider.shared.ptcl
import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.Http
import org.scalajs.dom.html._
import org.scalajs.dom.raw.MouseEvent
import io.circe.syntax._
import io.circe.generic.auto._
import scalatags.JsDom.short._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * User: TangYaruo
  * Date: 2018/1/30
  * Time: 14:46
  */
object PostsInfo extends Page {
  override def locationHash: String = ""

  /*背景图片*/
  val background = div(*.style := "position:absolute;width:100%;height:100%;z-index:-1")(
    img(*.src := "/webSpider/static/img/binfo.jpg")
  ).render

  /*顶部导航*/
  val navBar = div(*.cls := "navbar navbar-default navbar-fixed-top", *.role := "navigation")(
    div(*.cls := "container-fluid")(
      div(*.cls := "navbar-header")(
        a(*.cls := "navbar-brand", *.href := "#", *.style := "font-family:Gabriola; font-size:3em")(
          "webSpider"
        )
      ),
      div(*.cls := "collapse navbar-collapse", *.id := "bs-example-navbar-collapse-1")(
        ul(*.cls := "nav navbar-nav")(
          li(
            a(*.href := Routes.adminHome)("主页")
          ),
          li(
            a(*.href := Routes.supervise)("爬虫监控")
          ),
          li(*.cls := "dropdown")(
            a(*.href := "#", *.cls := "dropdown-toggle", *.data("toggle") := "dropdown")(
              "信息查询",
              span(*.cls := "caret")
            ),
            ul(*.cls := "dropdown-menu", *.role := "menu")(
              li(a(*.href := Routes.boardInfo)("版面信息")),
              li(*.cls := "divider"),
              li(*.cls := "active")(a(*.href := "#")("帖子信息"))
            )
          )
        ),
        form(*.cls := "navbar-form navbar-right", *.role := "search")(
          div(*.cls := "form-group")(
            input(*.`type` := "text", *.cls := "form-control", *.placeholder := "更多信息")
          ),
          button(*.`type` := "submit", *.cls := "button button-glow button-border button-rounded button-primary button-small")("搜索")
        )
      )
    )
  ).render

  var showType: Int = 0
  var interval: Int = 4
  /*侧边导航栏*/
  val totalPosts = a(*.href := "#", *.cls := "list-group-item list-group-item-danger")("总帖数").render //0
  val editPosts = a(*.href := "#", *.cls := "list-group-item list-group-item-success")("被编辑贴数").render  //1
  val maxReply = a(*.href := "#",  *.cls := "list-group-item list-group-item-info")("被回复数最多").render //2
  val maxParticipants = a(*.href := "#", *.cls := "list-group-item list-group-item-warning")("参与人数最多").render //3
  val maxPostsBoards = a(*.href := "#", *.cls := "list-group-item list-group-item-danger")("发帖量最多版面").render //4
  val verticalNav = div(*.cls := "list-group")(
    totalPosts,
    editPosts,
    maxReply,
    maxParticipants,
    maxPostsBoards
  ).render

  /*水平时间栏*/
  val four = li(a(*.href := "#", *.fontSize := "1.2em", *.color := "white")("4小时")).render
  val six = li(a(*.href := "#", *.fontSize := "1.2em", *.color := "white")("6小时")).render
  val day = li(a(*.href := "#", *.fontSize := "1.2em", *.color := "white")("24小时")).render
  val horizontalNav: UList = ul(*.cls := "nav nav-tabs")(
    four,
    six,
    day
  ).render

  /*水平分类栏*/
  val total = li(a(*.href := "#", *.fontSize := "1.2em", *.color := "white")("总量")).render
  val latestThirty = li(a(*.href := "#", *.fontSize := "1.2em", *.color := "white")("近30天")).render
  val horizontalType = ul()(
    total,
    latestThirty
  ).render

  /*总帖数搜索框*/
  val today: String = timestamp2Date(System.currentTimeMillis())
  val searchInput: Input = input(*.id := "date", *.cls := "form-control", *.placeholder := "请输入日期，格式：" + today, *.`type` := "text").render
  val searchButton: Button = button(*.cls := "btn btn-primary")("搜索").render
  val search: Div = div(*.cls := "input-group", *.marginBottom := "10px")(
    searchInput,
    span(*.cls := "input-group-btn")(
      searchButton
    )
  ).render

  /*编辑帖子搜索框*/
  val editSearchInput: Input = input(*.id := "date", *.cls := "form-control", *.placeholder := "请输入日期，格式：" + today, *.`type` := "text").render
  val editSearchButton: Button = button(*.cls := "btn btn-primary")("搜索").render
  val editSearch: Div = div(*.cls := "input-group", *.marginBottom := "10px")(
    editSearchInput,
    span(*.cls := "input-group-btn")(
      editSearchButton
    )
  ).render

  /*贴数显示*/
  val totalNum = h1(*.style := "font-family:YouYuan; font-size:1.5em; color: white").render
  val searchNum = h1(*.style := "font-family:YouYuan; font-size:1.5e; color:white").render
  val editTotalNum = h1(*.style := "font-family:YouYuan; font-size:1.5e; color:white").render
  val editSearchNum = h1(*.style := "font-family:YouYuan; font-size:1.5e; color:white").render



  totalPosts.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      showType = 0
      rightArea.appendChild(horizontalType)
      showRightArea
  }

  editPosts.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      showType = 1
      rightArea.appendChild(horizontalType)
      showRightArea
  }

  maxReply.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      showType = 2
      rightArea.appendChild(horizontalNav)
      showRightArea
  }

  maxParticipants.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      showType = 3
      rightArea.appendChild(horizontalNav)
      showRightArea
  }

  maxPostsBoards.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      showType = 4
      rightArea.appendChild(horizontalNav)
      showRightArea
  }

  four.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      interval = 4
      showRightArea
  }

  six.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      interval = 6
      showRightArea
  }

  day.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      interval = 24
      showRightArea
  }

  val rightArea =   div(*.cls := "col-lg-9")().render


  def showRightArea = {
    showType match {
      case 0 =>
        //显示总帖数，近30天每日贴数，搜索框查询某日贴数
//        val cat = img(*.src := "/webSpider/static/img/sun.gif").render
        rightArea.appendChild(waitGif)
        rightArea.appendChild(waitWarning)
      case 1 =>
        //显示被编辑总帖数，近30天每日被编辑贴数，搜索框查询某日被编辑的帖子数
        rightArea.appendChild(waitGif)
        rightArea.appendChild(waitWarning)
      case 2 =>
        //分页显示被回复数最多的帖子
        getPosts(1)
      case 3 =>
        //分页显示参与人数最多的帖子
        getPosts(0)
      case 4 =>
        //分页显示发帖量最多的版面
        getMaxPostsBoards
    }
  }

  /*加载中等待动画*/
  val waitGif = img(*.src := "/webSpider/static/img/sun.gif", *.width := "10px", *.height := "10px").render
  val waitWarning = h1(*.style := "font-family:YouYuan; font-size:1.5em; color: white")(
    "此功能在优化中。。。。"
  ).render



  def getPosts(postType: Int) = {
    val postReqParam = ptcl.PostsReq(postType, interval).asJson.noSpaces
    Http.postJsonAndParse[ptcl.PostRsp](Routes.searchPost, postReqParam).map {
      case Right(rst) =>
        makePostsTable(rst.posts)
        if (rightArea.lastChild != horizontalNav) {
          rightArea.removeChild(rightArea.lastChild)
        }
        rightArea.appendChild(tableDom)
      case Left(error) =>
        print(s"Json parse error: $error")
    }
  }

  def getMaxPostsBoards = {
    val boardsReqParam = ptcl.MaxPostsBoardsReq(interval).asJson.noSpaces
    Http.postJsonAndParse[ptcl.MaxPostsBoardsRsp](Routes.searchMaxPostsBoards, boardsReqParam).map {
      case Right(rst) =>
        makeBoardsTable(rst.boards)
        if (rightArea.lastChild != horizontalNav) {
          rightArea.removeChild(rightArea.lastChild)
        }
        rightArea.appendChild(boardsTableDom)
      case Left(error) =>
        print(s"Json parse error: $error")
    }
  }


  /*帖子列表*/
  val tableDom = table(*.cls := "table table-hover").render
  val tableHeadDom: TableSection = if (showType == 2) {
    thead(
      tr(*.cls := "active")(
        th(*.textAlign.center)("帖子Id"),
        th(*.textAlign.center)("所属版面"),
        th(*.textAlign.center)("标题"),
        th(*.textAlign.center)("作者"),
        th(*.textAlign.center)("回复人数"),
      )
    ).render
  } else {
    thead(
      tr(*.cls := "active")(
        th(*.textAlign.center)("帖子Id"),
        th(*.textAlign.center)("所属版面"),
        th(*.textAlign.center)("标题"),
        th(*.textAlign.center)("作者"),
        th(*.textAlign.center)("参与人数"),
      )
    ).render
  }

  def makePostsTable(posts: List[ptcl.Post]) = {
    val tableBodyDom = tbody.render
    posts.foreach {
      p =>
        val tableTr = tr(
          td(*.textAlign.center)(p.postId),
          td(*.textAlign.center)(p.boardName),
          td(*.textAlign.center)(p.title),
          td(*.textAlign.center)(p.author),
          td(*.textAlign.center)(p.participants),
        ).render
        tableBodyDom.appendChild(tableTr)
    }
    tableDom.appendChild(tableHeadDom)
    tableDom.appendChild(tableBodyDom)
  }

  /*版面列表*/
  val boardsTableDom = table(*.cls := "table table-hover").render
  val boardsTableHeadDom =  thead(
    tr(*.cls := "active")(
      th(*.textAlign.center)("版面英文名"),
      th(*.textAlign.center)("版面中文名"),
      th(*.textAlign.center)("发帖数")
    )
  ).render

  def makeBoardsTable(boardsList: List[ptcl.MaxPostsBoard]) = {
    val boardsTableBodyDom = tbody.render
    boardsList.foreach {
      b =>
        val tableTr = tr(
          td(*.textAlign.center)(b.boardName),
          td(*.textAlign.center)(b.boardNameCn),
          td(*.textAlign.center)(b.postNum)
        ).render
        boardsTableBodyDom.appendChild(tableTr)
    }
    boardsTableDom.appendChild(boardsTableHeadDom)
    boardsTableDom.appendChild(boardsTableBodyDom)
  }

  val app: Div = div(*.id := "app", *.height := "100%")(
    background,
    navBar,
    div(*.cls := "container")(
      div(*.cls := "row", *.marginTop := "100px")(
        div(*.cls := "col-lg-3")(
          br,
          br,
          br,
          verticalNav
        ),
        rightArea
      )
    )
  ).render

  override protected def build(): Div = {
    app
  }
}
