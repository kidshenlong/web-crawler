import scoverage.ScoverageSbtPlugin.autoImport._
import sbt.Keys._
import sbt._
import Dependencies._

/**
  * Created by Michael on 24/07/2016.
  */
object CommonSettings {


  lazy val coverageSettings = Seq(
    coverageMinimum := 90,
    coverageFailOnMinimum := true,
    coverageExcludedPackages := "mpm.Main;mpm.util.HttpClient;mpm.util.FileHelper"
  )

  lazy val commonSettings = Seq(
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature"),
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "-u", "console", "junitxml"),
    testOptions in Test += Tests.Setup(() => System.setProperty("specs2.junit.outDir", "target/reports")),
    libraryDependencies ++= dependencies,
    dependencyOverrides ++= libDependencyOverrides
    //test in assembly := {},
  ) ++ addCommandAlias("report", ";clean;coverage;test;coverageReport") ++ coverageSettings
}
