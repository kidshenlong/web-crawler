package mpm

import java.net.URL
import java.util.concurrent.Executors
import akka.pattern.{ask, pipe}
import akka.actor.{ActorRefFactory, ActorRef, Actor}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import mpm.Domain.Resource
import mpm.util.Helpers
import org.jsoup.Jsoup
import org.jsoup.nodes.{Element, Document}
import akka.http.scaladsl.model.StatusCodes._
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConversions._
import scala.concurrent.duration._
import akka.pattern.ask

/**
  * Created by Michael on 21/07/2016.
  */
class SlaveCrawler(val master: ActorRef, implicit val startUrl: URL) extends Actor
with Helpers{

  def actorRefFactory: ActorRefFactory = context
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  //Own execution context to manage blocking calls
  //Fixed size to handle potentially expensive calls
  //20's quite conservative. This is to avoid hitting OS thread limits
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(20))

  def receive = {
    case WorkAvailable() => master ! GiveWork()
    case Crawl(url) => handleCrawl(url)
    case GetUrlBody(url) => handleGetUrlBody(url) pipeTo sender()
    case ParseBody(body) => handleParseBody(body) pipeTo sender()
    case ExtractLinks(document) => handleExtractLinks(document) pipeTo sender()
    //case CrawlComplete(resources) => master
  }

  override def preStart(): Unit = {
    master ! GiveWork()
  }

  def handleCrawl(url: URL) = {
    println(s"[info] Started Crawling ${url.toString}")
    implicit val timeout = Timeout(60 seconds)
    val responseHtml = (self ? GetUrlBody(url)).mapTo[String]
    responseHtml.map { html =>
      val document = (self ? ParseBody(html)).mapTo[Document]
        document.map { doc =>
          val linkSeq = (self ? ExtractLinks(doc)).mapTo[Set[String]]

          linkSeq.map{ links =>
            val finalLinks = links.filter(isInternalLink)
              .map(makeAbsolute)
              .map(cleanLink)

            println(s"[info] Finished Crawling ${url.toString}")
            master ! CrawlComplete(Resource(url.toString, finalLinks, Set()))

          }

      }
    }
  }

  def handleGetUrlBody(url: URL): Future[String] = {
    def extractionLocation(httpResponse: HttpResponse): Future[String] = {
      val location = httpResponse.headers.find(l => l.is("location")).getOrElse(throw new scala.Exception()).value() //todo(mpm) handle exceptions
      handleGetUrlBody(new URL(location))
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

  def handleParseBody(body: String): Future[Document] = Future {
    Jsoup.parse(body)
  }

  def handleExtractLinks(doc: Document): Future[Set[String]] = Future {
    doc.select("a").toSet[Element].map(_.attr("href"))
  }

}
