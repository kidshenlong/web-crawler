package mpm.util

import java.net.URL

import akka.actor.{ActorSystem, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Future
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Michael on 22/07/2016.
  */
trait Helpers {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  def extractLinks(doc: Document): Future[List[String]] = Future {
    doc.select("a").toList.map(_.attr("href")).distinct
  }

  def extractStaticAssets(doc: Document): Future[List[String]] = Future {
    val imgElementsSrc = doc.select("img[src]").toList.map(_.absUrl("src"))

    val scriptElementsSrc = doc.select("script[src]").toList.map(_.absUrl("src"))

    val styleElementsHref = doc.select("link[rel=stylesheet]").toList.map(_.absUrl("href"))

    (imgElementsSrc ++ scriptElementsSrc ++ styleElementsHref).distinct
  }

  def parseHtml(body: String): Future[Document] = Future {
    Jsoup.parse(body)
  }

  def isInternalLink(url: String)(implicit startUrl: URL): Boolean = url match {
    case matchUrl if !matchUrl.contains("://") => true
    case matchUrl if matchUrl.startsWith(startUrl.toString) && !matchUrl.startsWith("#") => true
    case _ => false
  }

  def makeAbsolute(url: String)(implicit startUrl: URL): String = url match {
    case matchUrl if matchUrl.startsWith(startUrl.toString) => matchUrl
    case matchUrl if matchUrl.startsWith("/") => startUrl.toString + matchUrl
    case matchUrl => startUrl.toString + "/" + matchUrl
  }

  def cleanLink(url: String): String = url match {
    case matchUrl if matchUrl.endsWith("/") => cleanLink(url.dropRight(1))
    case matchUrl if matchUrl.contains("#") => cleanLink(matchUrl.split("#")(0))
    case _ => url
  }

}
