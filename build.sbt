name := "file-processor-concurrently-scala"

version := "0.1"

scalaVersion := "2.13.1"

val circeVersion = "0.12.1"

libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
