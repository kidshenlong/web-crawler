package mpm

/**
  * Created by Michael on 20/07/2016.
  */
object Domain {

  case class Link(protocol: String = "http", host: String, path: Option[String])

}
