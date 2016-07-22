package mpm

import java.net.URL

import mpm.Domain.Resource

sealed abstract class Message()

case class Start() extends Message()
case class CheckQueue() extends Message()
case class Idle() extends Message()
case class WorkAvailable() extends Message()
case class Crawl(url: URL) extends Message()
//case class CrawlComplete(urlCrawled: URL, urlsFound: Seq[URL])
case class CrawlComplete(resourcesFound: Resource) extends Message()
case class Finish() extends Message()
case class GiveWork() extends Message()
