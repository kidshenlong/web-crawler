package mpm

import java.net.URL
import java.util.concurrent.Executors

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import mpm.Domain.Resource
import mpm.util.{HttpClient, UrlHelper}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

//import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Michael on 21/07/2016.
  */
class SlaveCrawler(val master: ActorRef, implicit val startUrl: URL, httpClient: HttpClient)(implicit materializer: ActorMaterializer) extends Actor
with UrlHelper{

  implicit val system = context.system
  //Own execution context to manage blocking calls
  //Fixed size to handle potentially expensive calls
  //100's quite conservative. This is to avoid hitting OS thread limits
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(100))
  //implicit val ec = ExecutionContext.fromExecutor(Executors.newWorkStealingPool())

  var selfActorRef: ActorRef = self

  def receive = {
    case Crawl(url) => handleCrawl(url)
  }//todo Error handler to pipe failure back to master


  def handleCrawl(url: URL) = {
    println(s"[info] Started Crawling ${url.toString}")
    implicit val timeout = Timeout(15 seconds)
    val responseHtml = handleGetUrlBody(url)
    responseHtml.map { html =>
      val document = handleParseBody(html)
      document.map { doc =>
        val linkSet = handleExtractLinks(doc)

        linkSet.map{ links =>
          val finalLinks = links.filter(isInternalLink)
            .map(makeAbsolute)
            .map(cleanLink)

          val assetSet = handleExtractStaticAssets(doc)

          assetSet.map{ assets =>

            println(s"[info] Finished Crawling ${url.toString}")
            master ! CrawlComplete(Resource(url.toString, finalLinks, assets))

          }
        }
      }
    }
  }

  def handleGetUrlBody(url: URL): Future[String] = {
    /*def extractionLocation(httpResponse: HttpResponse): Future[String] = {
      val location = httpResponse.headers.find(l => l.is("location")).getOrElse(throw new scala.Exception()).value() //todo(mpm) handle exceptions
      handleGetUrlBody(new URL(location))
    }*/

    httpClient.sendRequest(HttpRequest(uri = url.toString)).flatMap{ httpResponse =>
      httpResponse.status match {
        /*case MovedPermanently => //Handle Redirects
          extractionLocation(httpResponse)
        case Found => //Handle Redirects
          extractionLocation(httpResponse)*/
          //todo (mpm) how can we follow redirects better?
        case _ => Unmarshal(httpResponse.entity).to[String]
      }
    }

  }

  def handleParseBody(body: String): Future[Document] = Future {
    Jsoup.parse(body)
  }

  def handleExtractLinks(doc: Document): Future[Set[String]] = Future {
    doc.select("a").toSet[Element].map(_.attr("href"))
  }

  def handleExtractStaticAssets(doc: Document): Future[Set[String]] = Future {
    val imgElementsSrc = doc.select("img[src]").toSet[Element].map(_.absUrl("src"))

    val scriptElementsSrc = doc.select("script[src]").toSet[Element].map(_.absUrl("src"))

    val styleElementsHref = doc.select("link[rel=stylesheet]").toSet[Element].map(_.absUrl("href"))

    imgElementsSrc ++ scriptElementsSrc ++ styleElementsHref
  }

}

object SlaveCrawler {
  def props(master: ActorRef, startUrl: URL, httpClient: HttpClient)(implicit materializer: ActorMaterializer) = Props(new SlaveCrawler(master, startUrl, httpClient))
}

