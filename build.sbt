organization in ThisBuild := "net.arbor"

description in ThisBuild := "Samza geostream"

scalaVersion in ThisBuild := "2.11.8"

resolvers in ThisBuild += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers in ThisBuild += "Bin tray" at "https://dl.bintray.com/arbornetworks/maven-private"

resolvers in ThisBuild += "confluent" at "http://packages.confluent.io/maven/"

bintrayOrganization in ThisBuild := Some("arbornetworks")

credentials in ThisBuild += Credentials(Path.userHome / ".bintray" / ".readonly")

version in ThisBuild := Process("./version.sh").lines.head.trim

crossScalaVersions in ThisBuild := Seq("2.10.6",  "2.11.8")
