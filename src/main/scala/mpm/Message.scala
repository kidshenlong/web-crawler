package mpm

import java.net.URL

import mpm.Domain.Resource
import org.jsoup.nodes.Document

sealed abstract class Message()

case object Start extends Message()
case class CheckQueue() extends Message()
case class Idle() extends Message()
case object WorkAvailable extends Message()
case class Crawl(url: URL) extends Message()
//case class CrawlComplete(urlCrawled: URL, urlsFound: Seq[URL])
case class CrawlComplete(resourcesFound: Resource) extends Message()
case class Finish(resources: scala.collection.mutable.Set[Resource]) extends Message()
case class GiveWork() extends Message()

case class GetUrlBody(url: URL) extends Message()
case class ParseBody(body: String) extends Message()
case class ExtractLinks(document: Document) extends Message()
case class ExtractStaticAssets(document: Document) extends Message()


case object Poll extends Message()
case object IsFinished extends Message()
