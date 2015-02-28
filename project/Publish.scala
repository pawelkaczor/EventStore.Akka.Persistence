import sbt._
import Keys._
import xerial.sbt.Sonatype.sonatypeSettings

object Publish {
  lazy val settings = sonatypeSettings :+ (pomExtra :=
     <scm>
      <url>git@github.com:pawelkaczor/EventStore.Akka.Persistence.git</url>
      <connection>scm:git:git@github.com:pawelkaczor/EventStore.Akka.Persistence.git</connection>
      <developerConnection>scm:git:git@github.com:pawelkaczor/EventStore.Akka.Persistence.git</developerConnection>
    </scm>
    <developers>
      <developer>
        <id>newicom</id>
        <name>Pawel Kaczor</name>
        <email>newion@o2.pl</email>
      </developer>
    </developers>)
}