package com.neo.sk.webSpider.utils

import java.util.concurrent.TimeUnit
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.chrome.ChromeDriver

/**
  * User: sky
  * Date: 2018/7/17
  * Time: 18:38
  */
object SeleniumClient {


  def fetch(url:String)={
    val options = new ChromeOptions
    options.addArguments("headless")// headless mode
    options.addArguments("disable-gpu")
    options.addArguments("blink-settings=imagesEnabled=false")
    System.setProperty("webdriver.chrome.driver","F:\\MyJava\\webSpider\\chromedriver.exe");//chromedriver服务地址
    val driver =new ChromeDriver(options); //新建一个WebDriver 的对象，但是new 的是FirefoxDriver的驱动
    driver.get(url);//打开指定的网站
    import java.util.concurrent.TimeUnit
    try{
      /**
        * WebDriver自带了一个智能等待的方法。
        *dr.manage().timeouts().implicitlyWait(arg0, arg1）；
        * Arg0：等待的时间长度，int 类型 ；
        * Arg1：等待时间的单位 TimeUnit.SECONDS 一般用秒作为单位。
        */
      driver.manage.timeouts.implicitlyWait(30, TimeUnit.SECONDS)
      val a=driver.getPageSource()
      driver.close()
      driver.quit()
      a
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
        driver.close()
        driver.quit()
        println(url+"---timeout")
        "timeout"
    }
  }

  def main(args: Array[String]): Unit = {
    val a=fetch("http://muse.jhu.edu/article/191023")
//    println(a)
//    MuseClient.parseIssueList(a)
//    MuseClient.parseArticleList(a)
    val b=MuseClient.parseArticleFull(a)
    println(b)
    Thread.sleep(30000)
  }
}
