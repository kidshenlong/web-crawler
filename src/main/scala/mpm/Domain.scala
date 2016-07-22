package mpm

/**
  * Created by Michael on 22/07/2016.
  */
object Domain {

  case class Resource(path: String, links: List[String], assets: List[String])

}
