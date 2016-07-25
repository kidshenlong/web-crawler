package mpm

import java.net.URL

import mpm.Domain.Resource

sealed abstract class Message()

case object Start extends Message()
case object WorkAvailable extends Message()
case class Crawl(url: URL) extends Message()
case class CrawlComplete(resourcesFound: Resource) extends Message()
case class Finish(resources: scala.collection.mutable.Set[Resource]) extends Message()