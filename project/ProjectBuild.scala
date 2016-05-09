import _root_.bintray.BintrayKeys._
import sbt.Keys._
import sbt._
import xerial.sbt.Pack._

object ProjectBuild extends sbt.Build with Tasks {
  val vSamza          = "0.10.0"
  val vHadoop         = "2.6.1"
  val vScalaCheck     = "1.13.0"
  val vKafkaAvro      = "2.0.1"

  val `networking-core`      = "net.arbor"            %%  "networking-core"               % "2.1.1-132"
  val `scalaz-core`          = "net.arbor"            %%  "scalaz-core"                   % "3.0.0-137"
  val `config-core`          = "net.arbor"            %%  "config-core"                   % "2.0.0-3"
  val `connect-geo-contract` = "net.arbor"            %%  "kafka-connect-geo-contract"    % "1.0.0-171"
  val `event-core`           = "net.arbor"            %%  "event-core"                    % "0.1.1-10"

  val `avro`                = "org.apache.avro"       %   "avro"                          % "1.8.0"
  val `samza-api`           = "org.apache.samza"      %   "samza-api"                     % vSamza
  val `samza-core`          = "org.apache.samza"      %%  "samza-core"                    % vSamza
  val `samza-kafka`         = "org.apache.samza"      %%  "samza-kafka"                   % vSamza
  val `samza-kv-rocksdb`    = "org.apache.samza"      %%  "samza-kv-rocksdb"              % vSamza
  val `samza-log4j`         = "org.apache.samza"      %   "samza-log4j"                   % vSamza
  val `samza-yarn`          = "org.apache.samza"      %%  "samza-yarn"                    % vSamza  excludeAll ExclusionRule(organization = "javax.servlet")
  val `jackson-mapper-asl`  = "org.codehaus.jackson"  %   "jackson-mapper-asl"            % "1.9.13"
  val `slf4j-simple`        = "org.slf4j"             %   "slf4j-simple"                  % "1.7.6"

  val `specs2-core`         = "org.specs2"            %%  "specs2-core"                   % "3.7.2"
  val `specs2-scalacheck`   = "org.specs2"            %%  "specs2-scalacheck"             % "3.7.2"
  val `scala-check`         = "org.scalacheck"        %%  "scalacheck"                    % vScalaCheck

  val avroSerde             = "io.confluent"          %   "kafka-avro-serializer"         % vKafkaAvro
  val rocksdb               = "org.rocksdb"           %   "rocksdbjni"                    % "4.4.1"
  val hadoopAws             = "org.apache.hadoop"     %   "hadoop-aws"                    % vHadoop

  implicit class ProjectOps(self: Project) {
    def noBintray = {
      self
        .settings(bintrayRelease := {})
        .settings(bintrayReleaseOnPublish := false)
        .settings(bintrayUnpublish := {})
    }

    def notPublished = {
      self
        .noBintray
        .settings(publish := {})
        .settings(publishArtifact := false)
    }

    def publishedToS3 = {
      self
        .noBintray
        .settings(publishTo := {
          lazy val branch = Process("git rev-parse --abbrev-ref HEAD").lines.head

          if (branch == "develop") {
            Some("Mayhem release artefacts" at "s3://staging-published-artefacts/maven/releases")
          } else {
            Some("Mayhem snapshot artefacts" at "s3://staging-published-artefacts/maven/snapshots")
          }
        })
        .settings(isSnapshot := true)
    }

    def publishedToBintray = {
      self
        .settings(scalacOptions in Test ++= Seq("-Yrangepos"))
    }

    def standard = {
      self
        .settings(bintrayOmitLicense := true)
        .settings(bintrayRepository := "maven-private")
        .settings(scalacOptions := Seq("-deprecation", "-encoding", "utf8"))
    }

    def genAvro = {
      self
        .settings(sbtavrohugger.SbtAvrohugger.specificAvroSettings)
        .settings((sourceDirectory in sbtavrohugger.SbtAvrohugger.avroConfig) := (resourceDirectory in Compile).value / "avro")
    }

    def samza = {
      self
          .settings(packAutoSettings)
          .settings(JobPropertiesFileTask.setting)
          .settings(PackageTgzTask.setting)
          .settings(shellPrompt := { s => Project.extract(s).currentProject.id + " > " })
          .settings(packagedArtifacts := {
            val output = name.value

            packagedArtifacts.value
                .updated(Artifact(output, "samza-job"   , "tar.gz"    ), PackageTgzTask.key.value)
                .updated(Artifact(output, "samza-config", "properties"), JobPropertiesFileTask.key.value)
          })
    }

    def libs(modules: ModuleID*) = self.settings(libraryDependencies ++= modules)

    def testLibs(modules: ModuleID*) = self.libs(modules.map(_ % "test"): _*)

    def it = self.configs(IntegrationTest).settings(Defaults.itSettings: _*)
  }

  lazy val `geostream-contract` = Project("geostream-contract", file("geostream-contract"))
      .standard.genAvro.publishedToBintray
      .libs(avro)

  lazy val all = Project("samza-geostream-multi", file("."))
      .standard.notPublished
      .aggregate(`geostream-contract`)
}
