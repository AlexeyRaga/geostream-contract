import sbt.Keys._
import sbt.{KeyRanks, _}
import xerial.sbt.Pack._

trait Tasks {
  object PackageTgzTask {
    val key = TaskKey[File]("packageJob", "Samza: produces a tarball artifact for our job", rank = KeyRanks.ATask)
    val setting = key := {
      val log = streams.value.log
      val packPath = pack.value
      val shellPath = new File("./lib/samza-shell-0.10.0-dist.tgz")
      val binPath = packPath / "bin"

      binPath.mkdir()

      log.info(s"Extracting ${shellPath} to ${binPath}")
      shellPath #> s"tar xz -C ${binPath}" ! log

      val output = (target in Compile).value / s"${name.value}-dist.tar.gz"

      s"tar zc -C ${packPath.getCanonicalPath} ./" #> output !

      output
    }
  }

  object JobPropertiesFileTask {
    val key = TaskKey[File]("jobPropertiesFile", "Samza: produces the job properties file", rank = KeyRanks.ATask)
    val setting = key := (baseDirectory in Compile).value / "config/samzaJob.properties"
  }
}
