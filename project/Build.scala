import sbt._
import Keys._
import sbtrelease.ReleasePlugin._

object Build extends Build {
  lazy val basicSettings = Seq(
    name                 := "akka-persistence-eventstore",
    organization         := "pl.newicom.dddd",
    scalaVersion         := "2.11.6",
    licenses             := Seq("BSD 3-Clause" -> url("http://raw.github.com/EventStore/EventStore.Akka.Persistence/master/LICENSE")),
    homepage             := Some(new URL("http://github.com/pawelkaczor/EventStore.Akka.Persistence")),
    description          := "Event Store Plugin for Akka Persistence",
    startYear            := Some(2013),
    scalacOptions        := Seq("-encoding", "UTF-8", "-unchecked", "-deprecation", "-feature", "-Xlint"),
    publishMavenStyle    := true,
    resolvers            ++= Seq("Akka Snapshot Repository" at "http://repo.akka.io/snapshots/", "Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots"),
    libraryDependencies ++= Seq(
      Akka.persistence, Akka.testkit, Akka.persistenceTck, eventstore, specs2, json4s))

  object Akka {
    val persistence    = apply("persistence-experimental")
    val persistenceTck = "com.typesafe.akka" %% "akka-persistence-tck-experimental" % "2.3.9" % "test"
    val testkit        = apply("testkit") % "test"

    private def apply(x: String) = "com.typesafe.akka" %% s"akka-$x" % "2.4-M1"
  }

  val eventstore = "pl.newicom.dddd" %% "eventstore-client" % "2.0.3-M1"
  val specs2     = "org.specs2" %% "specs2-core" % "2.4.15" % "test"
  val json4s     = "org.json4s" %% "json4s-native" % "3.2.11"

  def integrationFilter(name: String): Boolean = name endsWith "IntegrationSpec"
  def specFilter(name: String): Boolean = (name endsWith "Spec") && !integrationFilter(name)

  lazy val IntegrationTest = config("it") extend Test

  lazy val root = Project(
    "akka-persistence-eventstore",
    file("."),
    settings = basicSettings ++ Defaults.coreDefaultSettings ++ releaseSettings ++ Scalariform.settings ++ Publish.settings)
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.testTasks): _*)
    .settings(
    testOptions       in Test            := Seq(Tests.Filter(specFilter)),
    testOptions       in IntegrationTest := Seq(Tests.Filter(integrationFilter)),
    parallelExecution in IntegrationTest := false)
}