name          := "SoundDesigner"

version       := "0.2.0-SNAPSHOT"

organization  := "de.sciss"

description   := "Interactive Sound Design Tool"

homepage      := Some(url("https://github.com/iem-projects/SoundDesigner"))

licenses      := Seq("GPL v2+" -> url("http://www.gnu.org/licenses/gpl-2.0.txt"))

// ---- scala compiler settings and libraries ----

scalaVersion  := "2.11.7"

crossScalaVersions := Seq("2.11.7", "2.10.5")

libraryDependencies ++= Seq(
  "de.sciss" %% "soundprocesses"          % "2.21.1",
  "de.sciss" %% "scalacolliderswing"      % "1.25.2",         // some graphical features for ScalaCollider
  "de.sciss" %  "scalacolliderugens-spec" % "1.13.3",         // UGen specs used in the patcher class
  "de.sciss" %% "desktop"                 % "0.7.1"           // application framework
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture", "-encoding", "utf8")

// ---- runtime settings ----

initialCommands in console := """import at.iem.sysson._
import de.sciss.synth._
import ugen._
import Ops._
import de.sciss.osc.Implicits._
"""

// ---- publishing ----

publishMavenStyle := true

publishTo :=
  Some(if (isSnapshot.value)
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := { val n = name.value
<scm>
  <url>git@github.com:Sciss/{n}.git</url>
  <connection>scm:git:git@github.com:Sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
}

// ---- packaging (making standalones) ----

test in assembly := ()

target in assembly := baseDirectory.value    // make .jar file in the main directory

