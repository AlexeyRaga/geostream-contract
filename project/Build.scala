import _root_.bintray.BintrayKeys._
import sbt.Keys._
import sbt._

object Build extends sbt.Build with Tasks {
  val `avro`              = "org.apache.avro" %   "avro"                % "1.8.0"

  val `specs2-core`       = "org.specs2"      %%  "specs2-core"         % "3.7.2"
  val `specs2-scalacheck` = "org.specs2"      %%  "specs2-scalacheck"   % "3.7.2"
  val `scala-check`       = "org.scalacheck"  %%  "scalacheck"          % "1.13.0"

  implicit class ProjectOps(self: Project) {
    def notPublished = {
      self
        .settings(publish := {})
        .settings(publishArtifact := false)
    }

    def standard = {
      self
          .settings(bintrayOmitLicense := true)
          .settings(bintrayRepository := "maven-private")
          .settings(scalacOptions := Seq("-deprecation", "-encoding", "utf8"))
          .settings(scalacOptions in Test ++= Seq("-Yrangepos"))
    }

    def genAvro = {
      self
        .settings(sbtavrohugger.SbtAvrohugger.specificAvroSettings)
        .settings((sourceDirectory in sbtavrohugger.SbtAvrohugger.avroConfig) := (resourceDirectory in Compile).value / "avro")
    }

    def libs(modules: ModuleID*) = self.settings(libraryDependencies ++= modules)

    def testLibs(modules: ModuleID*) = self.libs(modules.map(_ % "test"): _*)

    def it = self.configs(IntegrationTest).settings(Defaults.itSettings: _*)
  }

  lazy val `geostream-contract` = Project("geostream-contract", file("geostream-contract"))
      .standard.genAvro
      .libs(avro)

  lazy val all = Project("geostream-contract-multi", file("."))
      .standard.notPublished
      .aggregate(`geostream-contract`)
}
