package com.neo.sk.webSpider.front

import com.neo.sk.webSpider.front.common.PageSwitcher

import scala.scalajs.js

/**
  * User: Taoz
  * Date: 6/3/2017
  * Time: 1:03 PM
  */
object Main extends js.JSApp {

    @scala.scalajs.js.annotation.JSExport
    override def main(): Unit = {
      PageSwitcher.switchPageFirst()
    }

}
