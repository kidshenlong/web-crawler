name := "web_crawler"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.8",
  "org.jsoup"         % "jsoup"       % "1.9.2"
)