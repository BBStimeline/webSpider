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

  def addVolume(r:rVolumes)={
    db.run(tVolumes.returning(tVolumes.map(_.id)) += r)
  }

  def getUndoData={
    db.run(tIssues.filter(r=>r.union===1&&r.isDone===0).result)
  }

  def getAllData={
    db.run(tIssues.filter(r=>r.union===1&&r.isDone===0).map(r=>(r.id,r.issue)).result)
  }

  def updateVolume(id:String)={
    db.run(tVolumes.filter(_.id===id).map(_.isDone).update(1))
  }

  def updateIssue(id:String)={
    db.run(tIssues.filter(_.id===id).map(_.isDone).update(2))
  }

  def getVolume(num:Int)=db.run(tVolumes.filter(_.isDone===0).sortBy(_.id).take(1).result)

}
