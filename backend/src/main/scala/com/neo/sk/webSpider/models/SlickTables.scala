package com.neo.sk.webSpider.models

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object SlickTables extends {
  val profile = slick.jdbc.PostgresProfile
} with SlickTables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait SlickTables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = tArticles.schema ++ tIssues.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table tArticles
   *  @param id Database column id SqlType(varchar), PrimaryKey, Length(255,true), Default()
   *  @param issue Database column issue SqlType(varchar), Length(255,true), Default()
   *  @param title Database column title SqlType(varchar), Length(512,true), Default()
   *  @param authors Database column authors SqlType(text), Default()
   *  @param authorinfo Database column authorinfo SqlType(text), Default()
   *  @param mail Database column mail SqlType(text), Default()
   *  @param page Database column page SqlType(varchar), Length(255,true), Default()
   *  @param abs Database column abs SqlType(text), Default()
   *  @param index Database column index SqlType(text), Default()
   *  @param fulltext Database column fulltext SqlType(varchar), Length(255,true), Default()
   *  @param board Database column board SqlType(varchar), Length(255,true), Default()
   *  @param classify Database column classify SqlType(varchar), Length(255,true), Default()
   *  @param doi Database column doi SqlType(varchar), Length(255,true), Default()
   *  @param isDone Database column is_done SqlType(int4), Default(0)
   *  @param issueId Database column issue_id SqlType(varchar), Length(255,true), Default()
   *  @param union Database column union SqlType(int4), Default(0) */
  case class rArticles(id: String = "", issue: String = "", title: String = "", authors: String = "", authorinfo: String = "", mail: String = "", page: String = "", abs: String = "", index: String = "", fulltext: String = "", board: String = "", classify: String = "", doi: String = "", isDone: Int = 0, issueId: String = "", union: Int = 0)
  /** GetResult implicit for fetching rArticles objects using plain SQL queries */
  implicit def GetResultrArticles(implicit e0: GR[String], e1: GR[Int]): GR[rArticles] = GR{
    prs => import prs._
    rArticles.tupled((<<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table articles. Objects of this class serve as prototypes for rows in queries. */
  class tArticles(_tableTag: Tag) extends profile.api.Table[rArticles](_tableTag, "articles") {
    def * = (id, issue, title, authors, authorinfo, mail, page, abs, index, fulltext, board, classify, doi, isDone, issueId, union) <> (rArticles.tupled, rArticles.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(issue), Rep.Some(title), Rep.Some(authors), Rep.Some(authorinfo), Rep.Some(mail), Rep.Some(page), Rep.Some(abs), Rep.Some(index), Rep.Some(fulltext), Rep.Some(board), Rep.Some(classify), Rep.Some(doi), Rep.Some(isDone), Rep.Some(issueId), Rep.Some(union)).shaped.<>({r=>import r._; _1.map(_=> rArticles.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(varchar), PrimaryKey, Length(255,true), Default() */
    val id: Rep[String] = column[String]("id", O.PrimaryKey, O.Length(255,varying=true), O.Default(""))
    /** Database column issue SqlType(varchar), Length(255,true), Default() */
    val issue: Rep[String] = column[String]("issue", O.Length(255,varying=true), O.Default(""))
    /** Database column title SqlType(varchar), Length(512,true), Default() */
    val title: Rep[String] = column[String]("title", O.Length(512,varying=true), O.Default(""))
    /** Database column authors SqlType(text), Default() */
    val authors: Rep[String] = column[String]("authors", O.Default(""))
    /** Database column authorinfo SqlType(text), Default() */
    val authorinfo: Rep[String] = column[String]("authorinfo", O.Default(""))
    /** Database column mail SqlType(text), Default() */
    val mail: Rep[String] = column[String]("mail", O.Default(""))
    /** Database column page SqlType(varchar), Length(255,true), Default() */
    val page: Rep[String] = column[String]("page", O.Length(255,varying=true), O.Default(""))
    /** Database column abs SqlType(text), Default() */
    val abs: Rep[String] = column[String]("abs", O.Default(""))
    /** Database column index SqlType(text), Default() */
    val index: Rep[String] = column[String]("index", O.Default(""))
    /** Database column fulltext SqlType(varchar), Length(255,true), Default() */
    val fulltext: Rep[String] = column[String]("fulltext", O.Length(255,varying=true), O.Default(""))
    /** Database column board SqlType(varchar), Length(255,true), Default() */
    val board: Rep[String] = column[String]("board", O.Length(255,varying=true), O.Default(""))
    /** Database column classify SqlType(varchar), Length(255,true), Default() */
    val classify: Rep[String] = column[String]("classify", O.Length(255,varying=true), O.Default(""))
    /** Database column doi SqlType(varchar), Length(255,true), Default() */
    val doi: Rep[String] = column[String]("doi", O.Length(255,varying=true), O.Default(""))
    /** Database column is_done SqlType(int4), Default(0) */
    val isDone: Rep[Int] = column[Int]("is_done", O.Default(0))
    /** Database column issue_id SqlType(varchar), Length(255,true), Default() */
    val issueId: Rep[String] = column[String]("issue_id", O.Length(255,varying=true), O.Default(""))
    /** Database column union SqlType(int4), Default(0) */
    val union: Rep[Int] = column[Int]("union", O.Default(0))
  }
  /** Collection-like TableQuery object for table tArticles */
  lazy val tArticles = new TableQuery(tag => new tArticles(tag))

  /** Entity class storing rows of table tIssues
   *  @param id Database column id SqlType(varchar), PrimaryKey, Length(255,true), Default()
   *  @param issue Database column issue SqlType(varchar), Length(255,true), Default()
   *  @param isDone Database column is_done SqlType(int4), Default(0)
   *  @param union Database column union SqlType(int4), Default(0) */
  case class rIssues(id: String = "", issue: String = "", isDone: Int = 0, union: Int = 0)
  /** GetResult implicit for fetching rIssues objects using plain SQL queries */
  implicit def GetResultrIssues(implicit e0: GR[String], e1: GR[Int]): GR[rIssues] = GR{
    prs => import prs._
    rIssues.tupled((<<[String], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table issues. Objects of this class serve as prototypes for rows in queries. */
  class tIssues(_tableTag: Tag) extends profile.api.Table[rIssues](_tableTag, "issues") {
    def * = (id, issue, isDone, union) <> (rIssues.tupled, rIssues.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(issue), Rep.Some(isDone), Rep.Some(union)).shaped.<>({r=>import r._; _1.map(_=> rIssues.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(varchar), PrimaryKey, Length(255,true), Default() */
    val id: Rep[String] = column[String]("id", O.PrimaryKey, O.Length(255,varying=true), O.Default(""))
    /** Database column issue SqlType(varchar), Length(255,true), Default() */
    val issue: Rep[String] = column[String]("issue", O.Length(255,varying=true), O.Default(""))
    /** Database column is_done SqlType(int4), Default(0) */
    val isDone: Rep[Int] = column[Int]("is_done", O.Default(0))
    /** Database column union SqlType(int4), Default(0) */
    val union: Rep[Int] = column[Int]("union", O.Default(0))
  }
  /** Collection-like TableQuery object for table tIssues */
  lazy val tIssues = new TableQuery(tag => new tIssues(tag))
}
