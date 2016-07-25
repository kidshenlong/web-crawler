import sbt._

/**
  * Created by Michael on 24/07/2016.
  */
object Dependencies {


  lazy val dependencies = {
    Seq(
      "com.typesafe.akka" %% "akka-actor"             % "2.4.8",
      "com.typesafe.akka" %% "akka-testkit"             % "2.4.8",
      "com.typesafe.akka" %% "akka-http-core"         % "2.4.8",
      "com.typesafe.akka" %% "akka-http-experimental" % "2.4.8",
      "org.jsoup"         %  "jsoup"                  % "1.9.2",
      "com.netaporter"    %% "scala-uri"              % "0.4.14",
      "org.json4s"        %% "json4s-jackson"         % "3.4.0",
      "org.specs2"        %% "specs2-core"            % "3.8.4"   % "test",
      "org.specs2"        %% "specs2-mock"            % "3.8.4"   % "test",
      "org.specs2"        %% "specs2-junit"           % "3.8.4"   % "test"

    )
  }

  lazy val libDependencyOverrides: Set[ModuleID] = Set()
}
