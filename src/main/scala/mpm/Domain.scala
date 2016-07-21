package mpm

/**
  * Created by Michael on 20/07/2016.
  */
object Domain {

  //case class Link(protocol: String = "http", host: String, path: Option[String])

  case class Resource(path: String, links: List[String], assets: List[String])

}
