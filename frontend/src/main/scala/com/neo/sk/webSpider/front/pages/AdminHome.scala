package com.neo.sk.webSpider.front.pages

import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.{Http, JsFunc}
import com.neo.sk.webSpider.shared.ptcl
import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.{Http, JsFunc}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.MouseEvent
import io.circe.syntax._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global
import scalatags.JsDom.short._

/**
  * User: TangYaruo
  * Date: 2017/12/6
  * Time: 17:30
  */
object AdminHome extends Page {
  override def locationHash: String = ""

  val background = div(*.style := "position:absolute;width:100%;height:100%;z-index:-1")(
    img(*.src := "/webSpider/static/img/bg.jpg")
  ).render

  val navBar = div(*.cls := "navbar navbar-default navbar-fixed-top", *.role := "navigation")(
    div(*.cls := "container-fluid")(
      div(*.cls := "navbar-header")(
        a(*.cls := "navbar-brand", *.href := "#", *.style := "font-family:Gabriola; font-size:3em")(
          "webSpider"
        )
      ),
      div(*.cls := "collapse navbar-collapse", *.id := "bs-example-navbar-collapse-1")(
        ul(*.cls := "nav navbar-nav")(
          li(*.cls := "active")(
            a(*.href := "#")("主页")
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

  val greeting = h1(*.textAlign.center, *.fontFamily := "Gigi", *.fontSize := "100px")("Hello, manager!")

  val startButton = button(*.cls := "button button-glow button-border button-rounded button-primary", *.data("toggle") := "modal", *.data("target") := "#startModal")(
    "启动爬虫",
    i(*.cls := "fa fa-rocket", *.aria.hidden := "true")
  ).render

  val stopButton = button(*.cls := "button button-glow button-border button-rounded button-primary", *.data("toggle") := "modal", *.data("target") := "#stopModal")(
    "停止爬虫",
    i(*.cls := "fa fa-play", *.aria.hidden := "fa fa-play")
  ).render

  def startFunc() = {
    Http.getAndParseOld[ptcl.ComRsp](Routes.AdminService.start).map {
      case Right(_) =>
        JsFunc.alert("爬虫已启动！")

      case Left(error) =>
        print(s"Json parse error: $error")

    }
  }

  def stopFunc() = {
    Http.getAndParseOld[ptcl.ComRsp](Routes.AdminService.stop).map {
      case Right(_) =>
        JsFunc.alert("爬虫已停止")
      case Left(error) =>
        print(s"Json parse error: $error")
    }
  }
  val confirmButton = button(*.`type` := "button", *.cls := "btn btn-primary")("确认").render
  val closeButton = button(*.`type` := "button", *.cls := "btn btn-default", *.data("dismiss") := "modal")("取消").render
  confirmButton.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      closeButton.click()
      startFunc()
  }
  val startModal =  div(*.cls := "modal fade", *.id := "startModal", *.tabindex := "-1", *.role := "dialog", *.aria.hidden := "true")(
    div(*.cls := "modal-dialog")(
      div(*.cls := "modal-content")(
        div(*.cls := "modal-header")(
          button(*.`type` := "button", *.cls := "close", *.data("dismiss") := "modal", *.aria.hidden := "true")("×"),
          h4(*.cls := "modal-title", *.id := "myModalLabel")("Warning")
        ),
        div(*.cls := "modal-body")("是否确认启动爬虫？"),
        div(*.cls := "modal-footer")(
          closeButton,
          confirmButton
        )
      )
    )
  ).render

  val stopConfirmButton = button(*.`type` := "button", *.cls := "btn btn-primary")("确认").render
  val stopCloseButton = button(*.`type` := "button", *.cls := "btn btn-default", *.data("dismiss") := "modal")("取消").render
  stopConfirmButton.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      stopCloseButton.click()
      stopFunc()
  }
  val stopModal = div(*.cls := "modal fade", *.id := "stopModal", *.tabindex := "-1", *.role := "dialog", *.aria.hidden := "true")(
    div(*.cls := "modal-dialog")(
      div(*.cls := "modal-content")(
        div(*.cls := "modal-header")(
          button(*.`type` := "button", *.cls := "close", *.data("dismiss") := "modal", *.aria.hidden := "true")("×"),
          h4(*.cls := "modal-title", *.id := "myModalLabel")("Warning")
        ),
        div(*.cls := "modal-body")("是否确认停止爬虫？"),
        div(*.cls := "modal-footer")(
          stopConfirmButton,
          stopCloseButton
        )
      )
    )
  ).render

  override def build(): Div = {
    div(*.id := "app", *.height := "100%")(
      background,
      navBar,
      div(*.cls := "container", *.padding := "100px", *.height := "100%")(
        div(*.cls := "row")(
          div(*.cls := "col-md-12", *.padding := "30px", *.marginTop := "50px", *.textAlign.center)(
            greeting,
            br,
            startButton,
            br,
            br,
            stopButton
          )
        )
      ),
      startModal,
      stopModal
    ).render
  }

}
