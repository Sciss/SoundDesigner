import AssemblyKeys._

name          := "SoundDesigner"

version       := "0.1.0-SNAPSHOT"

organization  := "at.iem.sysson"

description   := "Interactive Sound Design Tool"

homepage      := Some(url("https://github.com/iem-projects/SoundDesigner"))

licenses      := Seq("GPL v2+" -> url("http://www.gnu.org/licenses/gpl-2.0.txt"))

// ---- scala compiler settings and libraries ----

scalaVersion  := "2.10.3"

libraryDependencies ++= Seq(
  // "de.sciss" %% "lucresynth"              % "2.0.+",
  "de.sciss" %% "soundprocesses"          % "2.0.+",
  // "de.sciss" %% "scalacollider"           % "1.10.+",         // client for SuperCollider
  "de.sciss" %% "scalacolliderswing"      % "1.10.+",         // some graphical features for ScalaCollider
  "de.sciss" %  "scalacolliderugens-spec" % "1.7.1+",         // UGen specs used in the patcher class
  "de.sciss" %% "desktop"                 % "0.3.2+"          // application framework
)

retrieveManaged := true

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

// ---- runtime settings ----

initialCommands in console := """import at.iem.sysson._
import de.sciss.synth._
import ugen._
import Ops._
import de.sciss.osc.Implicits._
"""

// ---- build info source generator ----

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq(name, organization, version, scalaVersion, description,
  BuildInfoKey.map(homepage) { case (k, opt) => k -> opt.get },
  BuildInfoKey.map(licenses) { case (_, Seq((lic, _))) => "license" -> lic }
)

buildInfoPackage := organization.value

// ---- publishing ----

publishMavenStyle := true

publishTo :=
  Some(if (version.value endsWith "-SNAPSHOT")
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := { val n = name.value
<scm>
  <url>git@github.com:iem-projects/{n}.git</url>
  <connection>scm:git:git@github.com:iem-projects/{n}.git</connection>
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

// windows/linux

seq(assemblySettings: _*)

test in assembly := ()

target in assembly := baseDirectory.value    // make .jar file in the main directory

// mac os x

seq(appbundle.settings: _*)

// appbundle.icon <<= (resourceDirectory in Compile, organization) { case (par, org) =>
//   val icn = org.split('.').foldLeft(par)(_ / _) / "icon512.png"
//   Some(icn)
// }

// appbundle.mainClass := Some("at.iem.sysson.sound.designer.Application")

appbundle.javaOptions += "-Xmx2048m"

appbundle.target := baseDirectory.value      // make .app bundle in the main directory
