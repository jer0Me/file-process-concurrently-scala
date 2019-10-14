name := "file-processor-concurrently-scala"

version := "0.1"

scalaVersion := "2.13.1"

val circeVersion = "0.12.1"
val log4CatsVersion = "1.0.0"
val doobieVersion = "0.8.4"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core",
  "org.tpolecat" %% "doobie-h2",
  "org.tpolecat" %% "doobie-hikari"
).map(_ % doobieVersion)

libraryDependencies += "io.scalaland" %% "chimney" % "0.3.3"