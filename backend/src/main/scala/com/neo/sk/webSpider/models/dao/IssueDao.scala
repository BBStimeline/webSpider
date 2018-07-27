package com.neo.sk.webSpider.models.dao

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

import com.neo.sk.webSpider.utils.DBUtil.db
import slick.jdbc.PostgresProfile.api._
import com.neo.sk.webSpider.models.SlickTables._
import com.neo.sk.webSpider.utils.SecureUtil

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * User: sky
  * Date: 2018/7/17
  * Time: 19:41
  */
object IssueDao {
  def addInfo(r:rIssues)={
    db.run(tIssues.returning(tIssues.map(_.id)) += r)
  }

  def getUndoData={
    db.run(tIssues.filter(_.isDone===0).result)
  }

  def main(args: Array[String]): Unit = {

  }
}
