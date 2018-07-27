package com.neo.sk.webSpider.front.pages

import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.{Http, Shortcut}
import org.scalajs.dom.html._
import scalatags.JsDom.short._
import com.neo.sk.webSpider.shared.ptcl
import com.neo.sk.webSpider.front.utils.MyUtil._
import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.{Http, Shortcut}
import io.circe.syntax._
import io.circe.generic.auto._
import org.scalajs.dom.raw.{MouseEvent, Node}
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.KeyboardEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scalatags.JsDom
/**
  * User: TangYaruo
  * Date: 2017/12/8
  * Time: 16:15
  */
object BoardInfo extends Page {

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
              li(*.cls := "active")(a(*.href := "#")("版面信息")),
              li(*.cls := "divider"),
              li(a(*.href := Routes.postsInfo)("帖子信息"))
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

  /*侧边导航栏*/
  val hotBoardsSpan = span(*.cls := "badge", *.width := "34px").render
  val normalBoardsSpan = span(*.cls := "badge").render
  val abandonBoardsSpan = span(*.cls := "badge").render
  val allBoardsSpan = span(*.cls := "badge").render
  val hotBoards = a(*.href := "#", *.cls := "list-group-item list-group-item-danger")(
    hotBoardsSpan,
    "热门版面"
  ).render

  val normalBoards = a(*.href := "#", *.cls := "list-group-item list-group-item-success")(
    normalBoardsSpan,
    "普通版面"
  ).render

  val abandonBoards = a(*.href := "#", *.cls := "list-group-item list-group-item-info")(
    abandonBoardsSpan,
    "废弃版面"
  ).render

  val allBoards = a(*.href := "#", *.cls := "list-group-item list-group-item-warning")(
    allBoardsSpan,
    "全部版面"
  ).render
  val listGroup = div(*.cls := "list-group")(
    hotBoards,
    normalBoards,
    abandonBoards,
    allBoards
  ).render

  /*版面列表*/
  case class TableLine(id: Long, boardName: String, boardNameCn: String, moderator: String, todayNew: Int)
  val tableDom = table(*.cls := "table table-hover").render
  val tableHeadDom = thead(
    tr(*.cls := "active")(
      th(*.textAlign.center)("id"),
      th(*.textAlign.center)("版面英文名"),
      th(*.textAlign.center)("版面中文名"),
      th(*.textAlign.center)("版主"),
      th(*.textAlign.center)("今日新帖数")
    )
  ).render

  /*页面按钮及页码*/
  var pageNum: Int = 1
  var curPage = 1
  val page = p(*.style := "font-family: Gabriola;font-size: 2em;").render
  val lastPageButton = button(*.cls := "button button-raised button-primary button-pill button-small")("<<上一页").render
  val nextPageButton = button(*.cls := "button button-raised button-primary button-pill button-small")("下一页>>").render
  val pageArea = div(*.cls := "col-lg-2", *.textAlign.center, *.paddingBottom := "2px")(
    page
  ).render

  /*分割线*/
  val dividingLine: HR = hr(*.style := "height:2px;border:none;border-top:2px dotted #185598;").render

  /*搜索框*/
  val today: String = timestamp2Date(System.currentTimeMillis())
  val searchInput: Input = input(*.id := "date", *.cls := "form-control", *.placeholder := "请输入日期，格式：" + today, *.`type` := "text").render
  val searchButton: Button = button(*.cls := "btn btn-primary")("搜索").render
  val search: Div = div(*.cls := "input-group", *.marginBottom := "10px")(
    searchInput,
    span(*.cls := "input-group-btn")(
      searchButton
    )
  ).render

  /*搜索框功能实现*/

    searchInput.onkeypress = {
      e: KeyboardEvent =>
        if (e.charCode == KeyCode.Enter) {
          e.preventDefault()
          searchButton.click()
        }
    }

    searchButton.onclick = {
      e: MouseEvent =>
        e.preventDefault()
        val date = searchInput.value
        val data = ptcl.HistoryPostsReq(date, boardName).asJson.noSpaces
        Http.postJsonAndParse[ptcl.HistoryPostsRsp](Routes.getHistoryPosts, data).map {
          case Right(rst) =>
              totalPosts.innerHTML = s"总帖数: ${rst.info.totalPosts}"
              mainPosts.innerHTML = s"主帖数: ${rst.info.mainPosts}"
              replyPosts.innerHTML = s"回帖数: ${rst.info.replyPosts}"
              infoRow.replaceChild(historyPostsArea, infoRow.lastChild)
          case Left(error) =>
            print(s"Json parse error: $error")

        }
    }

  /*选择类型按钮*/
  private var delayTimeLimit = "30min"
  val t30 = a( *.href := "#")("30min").render
  val t1h = a( *.href := "#")("1h").render
  val t2h = a( *.href := "#")("2h").render
  val t4h = a( *.href := "#")("4h").render
  val t6h = a( *.href := "#")("6h").render
  val t24h = a( *.href := "#")("24h").render
  t30.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      delayTimeLimit = t30.innerHTML
      getDelayAndShow
  }
  t1h.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      delayTimeLimit = t1h.innerHTML
      getDelayAndShow
  }
  t2h.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      delayTimeLimit = t2h.innerHTML
      getDelayAndShow
  }
  t4h.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      delayTimeLimit = t4h.innerHTML
      getDelayAndShow
  }
  t6h.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      delayTimeLimit = t6h.innerHTML
      getDelayAndShow
  }
  t24h.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      delayTimeLimit = t24h.innerHTML
      getDelayAndShow
  }
  val bt1 = button(*.`type` := "button", *.cls := "button button-primary")("当前主要信息查询").render
  val bt2 = button(*.`type` := "button", *.cls := "button button-primary")("历史发帖数目查询").render
  val optionBt = span(*.cls := "button-dropdown button-dropdown-primary", *.data("buttons") := "dropdown")(
    button(*.cls := "dropdown-toggle button button-primary", *.data("toggle") := "dropdown")(
      "历史滞后时间查询",
      span(*.cls := "caret")
    ),
    ul(*.cls := "dropdown-menu", *.role := "menu")(
      li(t30),
      li(*.cls := "divider"),
      li(t1h),
      li(*.cls := "divider"),
      li(t2h),
      li(*.cls := "divider"),
      li(t4h),
      li(*.cls := "divider"),
      li(t6h),
      li(*.cls := "divider"),
      li(t24h)
    )
  ).render
  val optionButtons: Div = div(*.cls := "button-group")(
    bt1,
    bt2,
    optionBt
  ).render

  bt1.onclick = {
    e: MouseEvent =>
      infoRow.replaceChild(mainInfoArea, infoRow.lastChild)
  }

  bt2.onclick = {
    e: MouseEvent =>
      infoRow.replaceChild(historyPostsArea, infoRow.lastChild)
  }

  private var boardName = ""
  def getDelayAndShow = {
    val data = ptcl.GetDelayReq(boardName, delayTimeLimit).asJson.noSpaces
    Http.postJsonAndParse[ptcl.GetDelayRsp](Routes.getDelayByTime, data).map {
      case Right(rst) =>
        val averageDl = rst.averageDelay.delay.round
        averageDelayHis.innerHTML = if (averageDl == 0) {
          "平均滞后时间: " + "(此时为后台统计信息更新时间点，请稍后再试~)"
        } else {
          "平均滞后时间: " + averageDl / 60000 + " min " +averageDl % 60000 / 1000 + " s " + averageDl % 60000 % 1000 + " ms"
        }
        infoRow.replaceChild(historyDelayArea, infoRow.lastChild)
      case Left(error) =>
        print(s"Json parse error: $error")
    }
  }

  /*版面信息区域*/
  val boardNameMain: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("版面名称： ——").render
  val boardNameDelay: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("版面名称:  ——").render
  val boardNameHis: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("版面名称:  ——").render
  val postNum: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("当日帖子数目: ——").render
  val averageDelayMain: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("平均滞后时间: ——").render
  val averageDelayHis: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("平均滞后时间: ——").render
  val lastUpdateTime: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("上次更新时间: ——").render
  val warning: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white").render
  val totalPosts: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("总帖数: ——").render
  val mainPosts: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("主帖数: ——").render
  val replyPosts: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white")("回帖数：——").render
  val warningArea: Div = div(*.cls := "col-md-12", *.textAlign.center)(
    warning
  ).render
  val mainInfoArea: Div =  div(*.cls := "col-md-5 col-md-offset-4", *.textAlign.left)(
    boardNameMain,
    postNum,
    averageDelayMain,
    lastUpdateTime
  ).render
  val historyDelayArea: Div = div(*.cls := "col-md-5 col-md-offset-4", *.textAlign.left)(
    boardNameDelay,
    averageDelayHis
  ).render
  val historyPostsArea = div(
    div(*.cls := "col-md-3 col-md-offset-4", *.textAlign.center, *.marginTop := "15px")(
      search
    ),
    div(*.cls := "col-md-5 col-md-offset-4", *.textAlign.left)(
      boardNameHis,
      totalPosts,
      mainPosts,
      replyPosts
    ).render
  ).render
  val infoRow: Div =  div(*.cls := "row")(
    div(*.cls := "col-md-12", *.textAlign.center)(
      optionButtons
    ),
    mainInfoArea
  ).render
  val boardInfoArea: Div = div().render

  /*清表*/
  def cleanTable = if (tableDom.lastChild != tableHeadDom) {
    tableDom.removeChild(tableDom.lastChild)
  }

  /*侧边导航栏按钮功能实现*/
  hotBoards.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      cleanTable
      boardType = 2
      sendPageRequest(boardType, 1)
      pgNum = 1
  }

  normalBoards.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      cleanTable
      boardType = 0
      sendPageRequest(boardType, 1)
      pgNum = 1
  }

  abandonBoards.onclick = {
    e: MouseEvent =>
      cleanTable
      boardType = -1
      sendPageRequest(boardType, 1)
      pgNum = 1
  }

  allBoards.onclick = {
    e:MouseEvent =>
      e.preventDefault()
      cleanTable
      boardType = 1
      sendPageRequest(boardType, 1)
      pgNum = 1
  }


  def getBoardsNum = {
    Http.getAndParseOld[ptcl.BoardsNumRsp](Routes.getBoardsNum).map {
      case Right(rst) =>
        hotBoardsSpan.innerHTML = rst.hot.toString
        normalBoardsSpan.innerHTML = rst.normal.toString
        abandonBoardsSpan.innerHTML = rst.abandon.toString
        allBoardsSpan.innerHTML = (rst.hot + rst.normal +rst.abandon).toString
      case Left(error) =>
        print(s"Json parse error: $error")
    }
  }


  /*请求版面列表数据（一页）*/
  private var boardType: Int = 1
  private var pgNum = 1
  def sendPageRequest(boardType: Int, pg: Int) = {
    val data = ptcl.BoardsListReq(boardType, pg).asJson.noSpaces
    Http.postJsonAndParse[ptcl.BoardsPageRsp](Routes.getBoardsPage, data).map {
      case Right(rst) =>
        pageNum = if (rst.totalNum % 10 == 0) {
          rst.totalNum match {
            case 0 => 1
            case _ => rst.totalNum / 10
          }
        } else rst.totalNum / 10 + 1
        curPage = pg
        page.innerHTML = s"$curPage/$pageNum"
        pageArea.replaceChild(page, pageArea.firstChild)
        makeBoardsTable(rst.boardsLs)
      case Left(error) =>
        print(s"Json parse error: $error")
    }
  }
  /*生成版面列表*/
  def makeBoardsTable(ls: List[ptcl.BoardInfo]): Unit = {
    val tableBodyDom = tbody.render
    val trList = scala.collection.mutable.ListBuffer[TableLine]()
    ls.foreach {
      r =>
        val line = TableLine(
          r.id,
          r.boardNameEn,
          r.boardNameCn,
          r.moderator,
          r.todayNew
        )
        trList.append(line)
    }
    trList.foreach {
      r =>
        val tableTr = makeTableTr(r)
        tableBodyDom.appendChild(tableTr)
    }
    tableDom.appendChild(tableHeadDom)
    tableDom.appendChild(tableBodyDom)
  }

  def makeTableTr(line: TableLine): TableRow = {
    val tableTr = tr(
      td(*.textAlign.center)(line.id),
      td(*.textAlign.center)(line.boardName),
      td(*.textAlign.center)(line.boardNameCn),
      td(*.textAlign.center)(line.moderator),
      td(*.textAlign.center)(line.todayNew)
    ).render
    tableTr.onclick = {
      e:MouseEvent =>
        e.preventDefault()
        boardName = line.boardName
        val data = ptcl.BoardInfoReq(line.id, line.boardName).asJson.noSpaces
        Http.postJsonAndParse[ptcl.BoardInfoRsp](Routes.getBoardInfo, data).map {
          case Right(rst) =>
            if (rst.boardStatistic.updateTime == -1L) {
              warning.innerHTML = "暂时没有该版面具体信息,请稍后重试~"
              infoRow.replaceChild(warningArea, infoRow.lastChild)
              showBoardInfo
            } else {
              boardNameMain.innerHTML = "版面名称: " + line.boardName
              boardNameHis.innerHTML = "版面名称: " + line.boardName
              boardNameDelay.innerHTML = "版面名称: " + line.boardName
              postNum.innerHTML = "当日帖子数目: " + rst.boardStatistic.todayPosts
              val averageDl = rst.boardStatistic.averageDelay.round
              averageDelayMain.innerHTML = if (averageDl == 0) {
                "平均滞后时间: " + "(此时为后台统计信息更新时间点，请稍后再试~)"
              } else {
                "平均滞后时间: " + averageDl / 60000 + " min " +averageDl % 60000 / 1000 + " s " + averageDl % 60000 % 1000 + " ms"
              }
              lastUpdateTime.innerHTML = if (rst.boardStatistic.updateTime == 0) {
                "上次更新时间: " + "最近无更新"
              } else {
                "上次更新时间: " + dataFormatDefault(rst.boardStatistic.updateTime)
              }
              if (app.lastChild != boardInfoArea) {
                showBoardInfo
              } else {
                infoRow.replaceChild(mainInfoArea, infoRow.lastChild)
              }
            }
          case Left(error) =>
            print(s"Json parse error: $error")
        }
    }
    tableTr
  }

  def showBoardInfo: Node = {
    boardInfoArea.appendChild(infoRow)
    app.appendChild(dividingLine)
    app.appendChild(boardInfoArea)
  }

  lastPageButton.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      if (curPage == 1) {
        Shortcut.alert("该页已经为第一页!")
      } else {
        tableDom.removeChild(tableDom.lastChild)
        pgNum -= 1
        sendPageRequest(boardType, pgNum)
      }
  }

  nextPageButton.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      if (curPage == pageNum) {
        Shortcut.alert("该页已经为最后一页!")
      } else {
        cleanTable
        pgNum += 1
        sendPageRequest(boardType, pgNum)
      }
  }

  val app: Div = div(*.id := "app", *.height := "100%")(
    background,
    navBar,
    div(*.cls := "container", *.paddingTop := "50px")(
      div(*.cls := "row")(
        div(*.cls := "col-md-4 col-md-offset-6")(
          h1(*.style := "font-family:Gabriola; font-size:5em")("Board  List")
        )
      )
    ),
    div(*.cls := "container")(
      div(*.cls := "row")(
        div(*.cls := "col-lg-3")(
          listGroup
        ),
        div(*.cls := "col-lg-9")(
          tableDom,
        )
      ),
      div(*.cls := "row", *.textAlign.center, *.style := "margin-top:10px;")(
        div(*.cls := "col-lg-6", *.paddingLeft := "450px")(
          lastPageButton
        ),
        pageArea,
        div(*.cls := "col-lg-4", *.paddingRight := "80px")(
          nextPageButton
        )
      )
    )
  ).render

  override protected def build(): Div = {
    getBoardsNum
    sendPageRequest(1, 1)
    app
  }
}
