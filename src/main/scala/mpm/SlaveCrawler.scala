package mpm

import java.net.URL
import java.util.concurrent.Executors
import akka.pattern.{ask, pipe}
import akka.actor.{ActorRefFactory, ActorRef, Actor}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import mpm.Domain.Resource
import mpm.util.Helpers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import akka.http.scaladsl.model.StatusCodes._
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
  * Created by Michael on 21/07/2016.
  */
class SlaveCrawler(val master: ActorRef, implicit val startUrl: URL) extends Actor
with Helpers{

  def actorRefFactory: ActorRefFactory = context
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(200))

  override def preStart {
    //master ! Idle()
  }

  def receive = {
    case Crawl(url) => handleCrawl(url)
  }

  def handleCrawl(url: URL) = {
    makeHttpRequest(url).flatMap{ body =>
      parseHtml(body).flatMap { doc =>
        println(s"[info] Parsing for ${url.toString} complete!")

        extractLinks(doc).flatMap { linkElementsHref =>

          val filteredLinks = linkElementsHref.filter(isInternalLink)
            .map(makeAbsolute)
            .map(cleanLink)

          extractStaticAssets(doc).map{ staticAssets =>
            Resource(url.toString, filteredLinks, staticAssets)
          }
        }
      }
    }.map( res => CrawlComplete(res))
  } pipeTo sender

  private def makeHttpRequest(url: URL):Future[String] = {


    def extractionLocation(httpResponse: HttpResponse): Future[String] = {
      val location = httpResponse.headers.find(l => l.is("location")).getOrElse(throw new scala.Exception()).value() //todo(mpm) handle exceptions
      makeHttpRequest(new URL(location))
    }

    Http().singleRequest(HttpRequest(uri = url.toString)).flatMap{ httpResponse =>
      httpResponse.status match {
        case MovedPermanently => //Handle Redirects
          extractionLocation(httpResponse)
        case Found => //Handle Redirects
          extractionLocation(httpResponse)
        case _ => Unmarshal(httpResponse.entity).to[String]
      }
    }


  }

}
