package com.neo.sk.webSpider.front.pages

import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.{Http, JsFunc, Shortcut}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.MouseEvent
import com.neo.sk.webSpider.shared.ptcl
import com.neo.sk.webSpider.front.Routes
import com.neo.sk.webSpider.front.common.Page
import com.neo.sk.webSpider.front.utils.{Http, JsFunc, Shortcut}
import io.circe.syntax._
import io.circe.generic.auto._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.KeyboardEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scalatags.JsDom.short._

/**
  * User: TangYaruo
  * Date: 2017/12/7
  * Time: 16:47
  */
object AdminLogin extends Page{
  override def locationHash: String = ""

  val background = div(*.style := "position:absolute;width:100%;height:100%;z-index:-1")(
    img(*.src := "/webSpider/static/img/bg.jpg")
  ).render

  val submitButton = span(*.cls := "button-wrap")(
    button(*.`type` := "submit", *.cls := "button button-pill button-raised button-small button-primary")("登陆")
  ).render

  val userNameInput = input(*.`type` := "text", *.cls := "form-control", *.placeholder := "用户名", *.id := "username").render

  val passwordInput =  input(*.`type` := "password", *.cls := "form-control", *.placeholder := "密码", *.id := "password").render

  val loginForm = form(*.role := "form")(
    div(*.cls := "form-group")(
      label(*.style := "color:white", *.`for` := "username")("用户名"),
      userNameInput
    ),
    div(*.cls := "form-group")(
      label(*.style := "color:white", *.`for` := "password")("密码"),
      passwordInput
    ),
    div(*.marginLeft := "115px")(
      submitButton
    )
  ).render

  passwordInput.onkeypress = {
    e: KeyboardEvent =>
      if (e.charCode == KeyCode.Enter) {
        e.preventDefault()
        submitButton.click()
      }
  }

  submitButton.onclick = {
    e: MouseEvent =>
      e.preventDefault()
      val userName = userNameInput.value
      val password = passwordInput.value
      if (userName.nonEmpty && password.nonEmpty) {
        val data = ptcl.AdminConfirm(userName, password).asJson.noSpaces
        Http.postJsonAndParse[ptcl.ComRsp](Routes.loginSubmit, data).map {
          case Right(rsp) =>
            rsp.errCode match {
              case 0 =>
                println(s"login request sent success, result: $rsp")
                Shortcut.redirect(Routes.adminHome)
              case 12001 =>
                JsFunc.alert("密码错误！")
              case 12002 =>
                JsFunc.alert("该管理员账户不存在！")
            }
          case Left(error) =>
            print(s"Json parse error: $error")
        }
      } else {
        JsFunc.alert("输入不能为空!")
      }
  }



  override protected def build(): Div = {
    div(*.id := "app", *.height := "100%")(
      background,
      div(*.cls := "container")(
        div(*.cls := "row")(
          div(*.cls := "col-md-12", *.textAlign.center, *.paddingTop := "100px")(
            h1(*.fontFamily := "Gabriola", *.fontSize := "100px", *.color := "white")("webSpider Manager")
          )
        ),
        div(*.cls := "row")(
         div(*.cls := "col-md-4 col-md-offset-4", *.padding := "20px")(
           loginForm
         )
        )
      )
    ).render
  }

}
