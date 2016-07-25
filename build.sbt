import CommonSettings._


lazy val root = (project in file(".")).
  enablePlugins(plugins.JvmPlugin).
  settings(commonSettings: _*).
  settings(
    name := "web-crawler",
    version := "1.0"
  )
