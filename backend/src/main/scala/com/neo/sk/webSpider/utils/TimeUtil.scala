package com.neo.sk.webSpider.utils

import java.text.SimpleDateFormat
import com.github.nscala_time.time._

/**
  * Created by Zhong on 2017/9/4.
  */
object TimeUtil {

  def date2TimeStamp(date: String): Long = {
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date).getTime
  }

  def date2TimeStamp4Likes(date: String): Long = {
    new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date).getTime
  }

  def timeStamp2Date(timestamp: Long): String = {
    new SimpleDateFormat("yyyyMMddHHmmss").format(timestamp)
  }

  def timestamp2DateOnly(timestamp: Long): String = {
    new SimpleDateFormat("yyyy/MM/dd").format(timestamp)
  }

  def timeStamp2yyyyMMdd(timestamp: Long): String = {
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp)
  }
}
