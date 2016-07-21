name := "web_crawler"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"             % "2.4.8",
  "com.typesafe.akka" %% "akka-http-core"         % "2.4.8",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.8",
  "org.jsoup"         %  "jsoup"                  % "1.9.2",
  "com.netaporter"    %% "scala-uri"              % "0.4.14",
  "org.json4s"        %% "json4s-jackson"         % "3.4.0"
)