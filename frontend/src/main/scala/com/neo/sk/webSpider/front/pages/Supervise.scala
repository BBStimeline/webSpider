package com.neo.sk.webSpider.front.pages

import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.{Http, JsFunc}
import com.neo.sk.webSpider.shared.ptcl
import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.{Http, JsFunc}
import org.scalajs.dom.html.{Button, Div, Paragraph}
import org.scalajs.dom.raw.MouseEvent
import io.circe.syntax._
import io.circe.generic.auto._
import scalatags.JsDom.short._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * User: TangYaruo
  * Date: 2018/1/25
  * Time: 14:27
  */
object Supervise extends Page {
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
          li(*.cls := "active")(
            a(*.href := "#")("爬虫监控")
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

  /*模态框*/
  val confirmButton = button(*.`type` := "button", *.cls := "btn btn-primary")("确认").render
  val closeButton = button(*.`type` := "button", *.cls := "btn btn-default", *.data("dismiss") := "modal")("取消").render
  confirmButton.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      closeButton.click()
      submitFunc()
  }
  val modal = div(*.cls := "modal fade", *.id := "proxyModal", *.tabindex := "-1", *.role := "dialog", *.aria.hidden := "true")(
    div(*.cls := "modal-dialog")(
      div(*.cls := "modal-content")(
        div(*.cls := "modal-header")(
          button(*.`type` := "button", *.cls := "close", *.data("dismiss") := "modal", *.aria.hidden := "true")("×"),
          h4(*.cls := "modal-title", *.id := "myModalLabel")("Warning")
        ),
        div(*.cls := "modal-body")("是否确认修改备用代理配置信息？"),
        div(*.cls := "modal-footer")(
          closeButton,
          confirmButton
        )
      )
    )
  ).render


  /*选择按钮组*/
  val bt1 = button(*.`type` := "button", *.cls := "button button-pill button-primary")("添加新版面").render
  val bt2 = button(*.`type` := "button", *.cls := "button button-pill button-primary")("备用代理配置").render
  val bt3 = button(*.`type` := "button", *.cls := "button button-pill button-primary")("任务速度配置").render
  val optionButtons = div(*.cls := "button-group")(
    bt1,
    bt2,
    bt3
  ).render

  bt1.onclick = {
    e: MouseEvent =>
      e.preventDefault()
    //显示输入板块部分
      app.replaceChild(addBoardArea, app.lastChild)
  }

  bt2.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      Http.getAndParseOld[ptcl.ProxyConfigInfoRsp](Routes.checkProxyConfig).map {
        case Right(rst) =>
          addGate.innerHTML = s"堆积任务数上限（使用备用代理）：${rst.info.addGate}"
          removeGate.innerHTML = s"堆积任务数下限（取消备用代理）：${rst.info.removeGate}"
          val mode = if (rst.info.backupProxyMode) "使用中" else "未使用"
          val available = if (rst.info.backupProxyAvailable) "是" else "否"
          backupProxyMode.innerHTML = s"备用代理模式：$mode"
          backupProxyAvailable.innerHTML = s"备用代理是否可用：$available"
          app.replaceChild(proxyInfoArea, app.lastChild)
        case Left(error) =>
          print(s"Json parse error: $error")
      }
  }
  
  bt3.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      Http.getAndParseOld[ptcl.MaxTaskNumberInfoRsp](Routes.checkMaxTaskNumber).map {
        case Right(rst) =>
          maxNumber.innerHTML = s"5秒内任务数限制为：${rst.info.maxNumber}"
          backupMaxNumber.innerHTML = s"备用代理状态下，5秒内任务数限制为：${rst.info.maxNumber}"
          maxNumberInput.value = ""
          backupLimitInput.value = ""
          app.replaceChild(maxTaskInfoArea, app.lastChild)
        case Left(error) =>
          print(s"Json parse error: $error")
      }
  }

  /*添加板块相关模块*/
  val boardNameInput = input(*.cls := "form-control").render
  val boardNameCnInput = input(*.cls := "form-control").render
  val inputLogin = input(*.`type` := "radio", *.name := "optionsRadiosinline",*.id := "optionsRadios1", *.value := "option1").render
  val inputNoLogin = input(*.`type` := "radio", *.name := "optionsRadiosinline", *.id := "optionsRadios2", *.value := "option2").render
  val hotBoard = input(*.`type` := "radio", *.name := "optionsRadiosinline", *.id := "optionsRadios1", *.value := "option1").render
  val normalBoard = input(*.`type` := "radio", *.name := "optionsRadiosinline", *.id := "optionsRadios2", *.value := "option2").render
  val addBoardButton = button(*.cls := "btn btn-default", *.data("toggle") := "modal", *.data("target") := "#addModal")("确认添加").render

  val boardForm = form(*.cls := "form-horizontal")(
    div(*.cls := "form-group")(
      label(*.`for` := "inputBoardName", *.cls := "col-sm-8 control-label")("版面名："),
      div(*.cls := "col-sm-4")(
        boardNameInput
      )
    ),
    div(*.cls := "form-group")(
      label(*.`for` := "inputBoardNameCn", *.cls := "col-sm-8 control-label")("版面中文名："),
      div(*.cls := "col-sm-4")(
        boardNameCnInput
      )
    ),
    div(*.cls := "form-group")(
      br,
      label(*.`for` := "isLogin", *.cls := "col-sm-8 control-label")("是否需要登陆："),
      div(*.cls := "col-sm-4")(
        label(*.cls := "radio-inline")(
          inputLogin
        )("是"),
        label(*.cls := "radio-inline")(
          inputNoLogin
        )("否")
      )
    ),
    div(*.cls := "form-group")(
      br,
      label(*.`for` := "isHot", *.cls := "col-sm-8 control-label")("版面类型："),
      div(*.cls := "col-sm-4")(
        label(*.cls := "radio-inline")(
          hotBoard
        )("热门版面"),
        label(*.cls := "radio-inline")(
          normalBoard
        )("普通版面")
      )
    )
  )

  /*添加板块确认模组框*/
  val confirmButton4Add = button(*.`type` := "button", *.cls := "btn btn-primary")("确认").render
  val closeButton4Add = button(*.`type` := "button", *.cls := "btn btn-default", *.data("dismiss") := "modal")("取消").render
  confirmButton4Add.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      closeButton4Add.click()
      confrimFunc()
  }
  val addModal = div(*.cls := "modal fade", *.id := "addModal", *.tabindex := "-1", *.role := "dialog", *.aria.hidden := "true")(
    div(*.cls := "modal-dialog")(
      div(*.cls := "modal-content")(
        div(*.cls := "modal-header")(
          button(*.`type` := "button", *.cls := "close", *.data("dismiss") := "modal", *.aria.hidden := "true")("×"),
          h4(*.cls := "modal-title", *.id := "myModalLabel")("Warning")
        ),
        div(*.cls := "modal-body")("确认添加该板块？"),
        div(*.cls := "modal-footer")(
          closeButton4Add,
          confirmButton4Add
        )
      )
    )
  ).render
  def confrimFunc() = {
    val boardName = boardNameInput.innerHTML
    val boardNameCn = boardNameCnInput.innerHTML
    val isHot = if (hotBoard.checked) 2 else if (normalBoard.checked) 0 else -1
    val needLogin = if (inputLogin.checked) 1 else if (inputNoLogin.checked) 0 else -1
    if (boardName == "" || boardNameCn == "" || isHot == -1 || needLogin == -1) {
      JsFunc.alert("版面信息必须填写完整!")
    } else {
      val data = ptcl.InsertBoardReq(boardName, boardNameCn, isHot, needLogin).asJson.noSpaces
      Http.postJsonAndParse[ptcl.ComRsp](data, Routes.addNewBoard).map {
        case Right(rst) =>
          if (rst.errCode == 0) {
            JsFunc.alert("添加成功！")
            bt1.click()
          }
        case Left(error) =>
          print(s"Json parse error: $error")
      }
    }
  }

  /*代理配置相关模块*/
  val addGate: Paragraph = p(*.`type` := "text", *.onkeyup := "(this.v=function(){this.value=this.value.replace(/[^0-9-]+/,'');}).call(this)\" onblur=\"this.v();",*.style := "font-family:YouYuan; font-size:1.5em;color:white").render
  val removeGate: Paragraph = p(*.`type` := "text" , *.onkeyup := "(this.v=function(){this.value=this.value.replace(/[^0-9-]+/,'');}).call(this)\" onblur=\"this.v();",*.style := "font-family:YouYuan; font-size:1.5em;color:white").render
  val backupProxyMode: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white").render
  val backupProxyAvailable: Paragraph = p(*.style := "font-family:YouYuan; font-size:1.5em;color:white").render
  val reConfig = a(*.href := "#")(
    p(*.style := "font-family:YouYuan; font-size:1.2em", *.paddingTop := "10px")("点击更改配置")
  ).render
  val proxyInfoArea: Div = div(*.cls := "container")(
    div(*.cls := "row")(
      div(*.cls := "col-md-5 col-md-offset-4", *.textAlign.left)(
        h1(*.style := "font-family:YouYuan; font-size:1.6em;color:white", *.paddingBottom := "15px")("当前代理配置:"),
        addGate,
        removeGate,
        backupProxyMode,
        backupProxyAvailable,
        reConfig
      )
    )
  ).render
  reConfig.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      app.replaceChild(proxyConfigArea, app.lastChild)
  }


  val addGateInput = input(*.cls := "form-control").render
  val removeGateInput = input(*.cls := "form-control").render
  val enableProxy = input(*.`type` := "radio", *.name := "optionsRadios", *.id := "optionsRadios1", *.value := "option1").render
  val disableProxy = input(*.`type` := "radio", *.name := "optionsRadios", *.id := "optionsRadios2", *.value := "option2").render
  val submitButton = button(*.cls := "btn btn-default", *.data("toggle") := "modal", *.data("target") := "#proxyModal")("确认更改").render
  val configForm = form(*.cls := "form-horizontal")(
    div(*.cls := "form-group")(
      label(*.`for` := "inputAddGate", *.cls := "col-sm-8 control-label")("堆积任务数上限："),
      div(*.cls := "col-sm-4")(
        addGateInput
      )
    ),
    div(*.cls := "form-group")(
      label(*.`for` := "inputRemoveGate", *.cls := "col-sm-8 control-label")("堆积任务数下限："),
      div(*.cls := "col-sm-4")(
        removeGateInput
      )
    ),
    div(*.cls := "form-group")(
      div(*.cls := "col-sm-offset-8 col-sm-4")(
        div(*.cls := "radio")(
          label(
            enableProxy,
            "允许使用备用代理"
          )
        ),
        div(*.cls := "radio")(
          label(
            disableProxy,
            "禁止使用备用代理"
          )
        )
      )
    )
  )

  def submitFunc() = {
    val addGateValue = if (addGateInput.value == "") None else Some(addGateInput.value.toInt)
    val removeGateValue = if (removeGateInput.value == "") None else Some(removeGateInput.value.toInt)
    val isAvailable = if (enableProxy.checked) Some(true) else if (disableProxy.checked) Some(false) else {
      None
    }
    val data = ptcl.ProxyConfig(addGateValue, removeGateValue, isAvailable).asJson.noSpaces
    Http.postJsonAndParse[ptcl.ComRsp](Routes.editProxyConfig, data).map {
      case Right(rst) =>
        if (rst.errCode == 0) {
          JsFunc.alert("请求提交成功！")
          bt2.click()
        }
      case Left(error) =>
        print(s"Json parse error: $error")
    }
  }
  
  /*任务速度相关模块*/
  val taskConfirmButton = button(*.`type` := "button", *.cls := "btn btn-primary")("确认").render
  val taskCloseButton = button(*.`type` := "button", *.cls := "btn btn-default", *.data("dismiss") := "modal")("取消").render
  taskConfirmButton.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      taskCloseButton.click()
      taskSubmitFunc()
  }
  val maxNumberModel = div(*.cls := "modal fade", *.id := "maxNumberModel", *.tabindex := "-1", *.role := "dialog", *.aria.hidden := "true")(
    div(*.cls := "modal-dialog")(
      div(*.cls := "modal-content")(
        div(*.cls := "modal-header")(
          button(*.`type` := "button", *.cls := "close", *.data("dismiss") := "maxNumberModel", *.aria.hidden := "true")("×"),
          h4(*.cls := "modal-title", *.id := "myModalLabel")("Warning")
        ),
        div(*.cls := "modal-body")("是否确认修改任务速度相关配置？"),
        div(*.cls := "modal-footer")(
          taskCloseButton,
          taskConfirmButton
        )
      )
    )
  ).render
  
  val maxNumber: Paragraph = p(*.`type` := "text", *.onkeyup := "(this.v=function(){this.value=this.value.replace(/[^0-9-]+/,'');}).call(this)\" onblur=\"this.v();",*.style := "font-family:YouYuan; font-size:1.5em;color:white").render
  val backupMaxNumber: Paragraph = p(*.`type` := "text", *.onkeyup := "(this.v=function(){this.value=this.value.replace(/[^0-9-]+/,'');}).call(this)\" onblur=\"this.v();",*.style := "font-family:YouYuan; font-size:1.5em;color:white").render
  val maxNumberInput = input(*.cls := "form-control", *.id := "maxNumberInput").render
  val backupLimitInput = input(*.cls := "form-control", *.id := "backupLimitInput").render
  val maxSubmit = button(*.cls := "btn btn-default", *.data("toggle") := "modal", *.data("target") := "#maxNumberModel")("修改").render
  val maxTaskInfoArea: Div = div(*.cls := "container")(
    div(*.cls := "row")(
      div(*.cls := "col-md-8 col-md-offset-4", *.textAlign.left)(
        h1(*.style := "font-family:YouYuan; font-size:1.6em;color:white", *.paddingBottom := "15px")("当前任务速度限制情况:"),
        maxNumber,
        form(*.cls := "form-horizontal")(
          div(*.cls := "form-group")(
            label(*.`for` := "maxNumberInput", *.cls := "col-sm-4 control-label", *.style := "font-family:YouYuan; font-size:1.5em; color:white; text-align:left;")("修改任务速度限制："),
            div(*.cls := "col-sm-4")(
              maxNumberInput
            )
          ),
          div(*.cls := "form-group")(
            label(*.`for` := "backupLimitInput", *.cls := "col-sm-4 control-label", *.style := "font-family:YouYuan; font-size:1.5em; color:white; text-align:left;")("修改备用代理速度："),
            div(*.cls := "col-sm-4")(
              backupLimitInput
            )
          )
        ),
        div(*.cls := "col-sm-offset-3 col-sm-4")(
          maxSubmit
        )
      )
    )
  ).render
  
  def taskSubmitFunc() = {
    val newMaxNumber: Option[Int] = if (maxNumberInput.value == "") {
      None
    } else {
      try {
        Some(maxNumberInput.value.toInt)
      } catch {
        case e: Exception =>
          Some(-1)
      }
    }
    val backupLimiter: Option[Int] = if (backupLimitInput.value == "") {
      None
    } else {
      try {
        Some(backupLimitInput.value.toInt)
      } catch {
        case e: Exception =>
          Some(-1)
      }
    }
    val data = ptcl.MaxTaskSetting(newMaxNumber, backupLimiter).asJson.noSpaces
    if (!newMaxNumber.contains(-1) && !backupLimiter.contains(-1)) {
      Http.postJsonAndParse[ptcl.ComRsp](Routes.changeMaxTaskNumber, data).map {
        case Right(rst) =>
          if (rst.errCode == 0) {
            JsFunc.alert("请求提交成功！")
            bt3.click()
          }
        case Left(error) =>
          print(s"Json parse error: $error")
      }
    } else {
      JsFunc.alert("填写的数字不合法")
    }
  }
  
  

  val proxyConfigArea: Div = div(*.cls := "container")(
    div(*.cls := "row")(
      div(*.cls := "col-md-8")(
        configForm,
        div(*.cls := "col-sm-offset-8 col-sm-4")(
          submitButton
        )
      )
    )
  ).render

  val addBoardArea = div(*.cls := "container")(
    div(*.cls := "row")(
      div(*.cls := "col-md-8")(
        boardForm,
        div(*.cls := "col-sm-offset-8 col-sm-4")(
          addBoardButton
        )
      )
    )
  ).render


  val app: Div = div(*.id := "app", *.height := "100%")(
    maxNumberModel,
    modal,
    addModal,
    background,
    navBar,
    div(*.cls := "container")(
      div(*.cls := "row")(
        div(*.cls := "col-md-12", *.padding := "80px", *.textAlign.center)(
          optionButtons
        )
      )
    ),
    div()
  ).render

  override protected def build(): Div = {
    app
  }
}
