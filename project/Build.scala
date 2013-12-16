import sbt._
import Keys._
import scala.Some

object BuildSettings {
  lazy val repoKind = SettingKey[String]("repo-kind", "Maven repository kind (\"snapshots\" or \"releases\")")

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.example",
    version := "1.0.0-SNAPSHOT",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalacOptions ++= Seq(),
    scalaVersion := "2.10.3",
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := {
      _ => false
    },
    makePomConfiguration ~= {
      _.copy(configurations = Some(Seq(Compile, Runtime)))
    },
    startYear := Some(2008),
    pomExtra :=
      <developers>
        <developer>
          <id>aparo</id>
          <name>Alberto Paro</name>
          <timezone>+1</timezone>
          <url></url>
        </developer>
      </developers>
        <scm>
          <url></url>
          <connection></connection>
        </scm>,
        addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0-M1" cross CrossVersion.full)
  )
}

object MyBuild extends Build {
  import BuildSettings._
  import Dependencies._
  lazy val root: Project = Project(
    "root",
    file("."),
    settings = buildSettings ++ Seq(
      run <<= run in Compile in core
    )
  ) aggregate(macros, core)

  lazy val macros: Project = Project(
    "example-macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(scalaReflect, liftJson, liftJsonExt, scalaz)
      )
  )

  lazy val core: Project = Project(
    "example-core",
    file("core"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(scalaReflect, liftJson, liftJsonExt, jacksonCore, jacksonDatabind, finagleHTTP,
        liftRecord, liftUtil, jacksonHibernate, jacksonJaxrs, jacksonScala, slf4jApi, log4jLib, log4jOverSlf4j,
        common_io, mockito, scalatest, specs2, scalaz, parboiled) 
    )
  ).dependsOn(macros).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)


}
