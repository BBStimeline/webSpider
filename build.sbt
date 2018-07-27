import xerial.sbt.Pack.{packExtraClasspath, packJvmOpts, packMain}

resolvers += Resolver.sonatypeRepo("snapshots")



val scalaV = "2.12.4"
//val scalaV = "2.11.8"


val projectName = "webSpider"
val projectVersion = "0.1.5"

val slickV = "3.2.1"
val akkaV = "2.5.11"
val akkaHttpV = "10.1.0"

val scalaXmlV = "1.0.6"
//val hikariCpV = "2.5.1"
val hikariCpV = "2.6.2"
//val logbackV = "1.1.7"
val logbackV = "1.2.3"
val scalikeJdbcV = "2.5.0"
//val nscalaTimeV = "2.14.0"
val nscalaTimeV = "2.16.0"
val codecV = "1.10"
val postgresJdbcV = "9.4.1208"
//val asyncHttpClientV = "2.0.24"
val asyncHttpClientV = "2.0.32"
//val ehCacheV = "2.10.3"
val ehCacheV = "2.10.4"


//val scalaJsDomV = "0.9.1"
val scalaJsDomV = "0.9.2"
val scalatagsV = "0.6.5"
val circeVersion = "0.8.0"
val diodeV = "1.1.0"

//crawler
val httpClientV= "4.5.3"
val httpCoreV= "4.4.6"
val jsoupVersion = "1.9.2"

val projectMainClass = "com.neo.sk." + projectName + ".Boot"

//val playComponentV = "2.5.9"
val javaxMailV = "1.5.6"

def commonSettings = Seq(
  version := projectVersion,
  scalaVersion := scalaV,
  scalacOptions ++= Seq(
    //"-deprecation",
    "-feature"
  )
)


lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(name := "shared")
  .settings(commonSettings: _*)


lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js


// Scala-Js frontend
lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(name := "frontend")
  .settings(commonSettings: _*)
  .settings(inConfig(Compile)(
    Seq(
      fullOptJS,
      fastOptJS,
      //      packageScalaJSLauncher,
      packageJSDependencies,
      packageMinifiedJSDependencies
    ).map(f => (crossTarget in f) ~= (_ / "sjsout"))
  ))
  .settings(skip in packageJSDependencies := false)
  .settings(
    //    persistLauncher in Compile := true,
    //    persistLauncher in Test := false,
    scalaJSUseMainModuleInitializer := false,
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "org.scala-js" %%% "scalajs-dom" % scalaJsDomV,
      "me.chrons" %%% "diode" % diodeV,
      //"com.lihaoyi" %%% "upickle" % upickleV,
      "com.lihaoyi" %%% "scalatags" % scalatagsV
      //"com.lihaoyi" %%% "utest" % "0.3.0" % "test"
    )
  )
  .dependsOn(sharedJs)


// Akka Http based backend
lazy val backend = (project in file("backend"))
  .settings(commonSettings: _*)
  .settings(
    mainClass in reStart := Some(projectMainClass),
    javaOptions in reStart += "-Xmx2g"
  )
  .settings(name := "backend")
  .settings(
    //pack
    // If you need to specify main classes manually, use packSettings and packMain
    packSettings,
    // [Optional] Creating `hello` command that calls org.mydomain.Hello#main(Array[String])
    packMain := Map(projectName -> projectMainClass),
    packJvmOpts := Map(projectName -> Seq("-Xmx512m", "-Xms128m")),
    packExtraClasspath := Map(projectName -> Seq("."))
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      //"org.scala-lang" %% "scala-reflect" % scalaV,
      "org.scala-lang.modules" %% "scala-xml" % scalaXmlV,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaV withSources(),
      "com.typesafe.akka" %% "akka-actor-typed" % akkaV withSources(),
      "com.typesafe.akka" %% "akka-slf4j" % akkaV,
      "com.typesafe.akka" %% "akka-stream" % akkaV,
      "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
      "com.typesafe.slick" %% "slick" % slickV withSources(),
      "com.typesafe.slick" %% "slick-codegen" % slickV,
      "org.scalikejdbc" %% "scalikejdbc" % scalikeJdbcV,
      "org.scalikejdbc" %% "scalikejdbc-config" % scalikeJdbcV,
      "com.lihaoyi" %% "scalatags" % scalatagsV,
      "com.github.nscala-time" %% "nscala-time" % nscalaTimeV,
      "com.zaxxer" % "HikariCP" % hikariCpV,
      "ch.qos.logback" % "logback-classic" % logbackV withSources(),
      "commons-codec" % "commons-codec" % codecV,
      "org.postgresql" % "postgresql" % postgresJdbcV,
      "org.asynchttpclient" % "async-http-client" % asyncHttpClientV,
      "net.sf.ehcache" % "ehcache" % ehCacheV,
      "org.apache.httpcomponents" % "httpclient" % httpClientV,
      "org.apache.httpcomponents" % "httpcore" % httpCoreV,
      "org.jsoup" % "jsoup" % jsoupVersion,
      "com.sun.mail" % "javax.mail" % javaxMailV,
      // https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java
      "org.seleniumhq.selenium" % "selenium-java" % "3.13.0"


    )
  )
  //  .settings {
  //    (resourceGenerators in Compile) += Def.task {
  //      val fastJsOut = (fastOptJS in Compile in frontend).value.data
  //      val fastJsSourceMap = fastJsOut.getParentFile / (fastJsOut.getName + ".map")
  //      Seq(
  //        fastJsOut,
  //        fastJsSourceMap
  //      )
  //    }.taskValue
  //  }
  .settings(
  (resourceGenerators in Compile) += Def.task {
    val fullJsOut = (fullOptJS in Compile in frontend).value.data
    val fullJsSourceMap = fullJsOut.getParentFile / (fullJsOut.getName + ".map")
    Seq(
      fullJsOut,
      fullJsSourceMap
    )
  }.taskValue)
  .settings(
    (resourceGenerators in Compile) += Def.task {
      Seq(
        (packageJSDependencies in Compile in frontend).value
        //(packageMinifiedJSDependencies in Compile in frontend).value
      )
    }.taskValue)
  .settings(
    (resourceDirectories in Compile) += (crossTarget in frontend).value,
    watchSources ++= (watchSources in frontend).value
  )
  .dependsOn(sharedJvm)



lazy val root = (project in file("."))
  .aggregate(frontend, backend)
  .settings(name := projectName)




