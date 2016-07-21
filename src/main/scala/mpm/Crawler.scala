package mpm

import java.io.{BufferedWriter, FileWriter, File}
import java.net.URL
import java.util.concurrent.Executors

import akka.actor.{Actor, ActorRefFactory}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import mpm.Domain.Resource
import org.json4s.DefaultFormats
import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpHeader
import org.json4s.jackson.Serialization.{read, write}

/**
  * Created by Michael on 20/07/2016.
  */
class Crawler(startUrl: URL) extends Actor{

  def actorRefFactory: ActorRefFactory = context
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(200))


  //In a real world situation this would be a call to a strongly consistent Database
  //or something function ie: Memoisation
  var crawled = scala.collection.mutable.Set[URL]()
  var queue = 0
  var resourcesObject = scala.collection.mutable.Set[Resource]()


  def receive: Receive = {
    case Start() => start()
    case Explore(url) => explore(url)
    case Finish() => finish()
  }

  def start(): Unit = {
    scrapeUrl(startUrl).map{ items =>
      items.foreach{ item =>
        self ! Explore(item)
      }
    }
  }

  def explore(url: URL): Unit = {
    scrapeUrl(url).map { items =>
      items.foreach { item =>
        if(!crawled.contains(item)) self ! Explore(item)
      }
    }.map { _ =>
      if(queue == 0) println(s"[info] Finished Scraping ${startUrl.toString}"); self ! Finish()
    }
  }

  def finish(): Unit = {
    saveToFile()
  }

  protected def scrapeUrl(url: URL): Future[List[URL]] = {
    println(s"[info] Scraping ${url.toString}")

    crawled += url

    queue = queue + 1


    makeHttpRequest(url).map{ body =>

      println(s"[info] Scraping ${url.toString} complete!")

      queue = queue - 1

      val doc = Jsoup.parse(body)

      val linkElements = doc.select("a").toList
      val linkElementsHref = linkElements.map(_.attr("href"))

      val filteredLinks = linkElementsHref.filter(isInternalLink)
        .map(makeAbsolute)
        .map(cleanLink)

      val imgElements = doc.select("img[src]").toList
      val imgElementsSrc = imgElements.map(_.absUrl("src"))

      val scriptElements = doc.select("script[src]").toList
      val scriptElementsSrc = scriptElements.map(_.absUrl("src"))

      val styleElements = doc.select("link[rel=stylesheet]").toList
      val styleElementsSrc = styleElements.map(_.absUrl("href"))

      resourcesObject += Resource(url.toString, filteredLinks, imgElementsSrc ++ scriptElementsSrc ++ styleElementsSrc)


      val linksToBeReturn = filteredLinks.map( l => new URL(l))
        .filter(li => if(crawled.contains(li)) false else true)

      linksToBeReturn.foreach(link => println(s"[info] Found ${link.toString}"))

      linksToBeReturn
    }

  }

  def makeHttpRequest(url: URL):Future[String] = {


    def extractionLocation(httpResponse: HttpResponse): Future[String] = {
      val location = httpResponse.headers.find(l => l.is("location")).getOrElse(throw new scala.Exception()).value() //todo(mpm) handle exceptions
      makeHttpRequest(new URL(location))
    }

    Http().singleRequest(HttpRequest(uri = url.toString)).flatMap{ httpResponse =>
      httpResponse.status match {
/*        case MovedPermanently => //Handle Redirects
          extractionLocation(httpResponse)
        case Found => //Handle Redirects
          extractionLocation(httpResponse)*/
        case _ => Unmarshal(httpResponse.entity).to[String]
      }
    }


  }

  def isInternalLink(url: String): Boolean = url match {
    case matchUrl if !matchUrl.startsWith("http") => true
    case matchUrl if matchUrl.startsWith(startUrl.toString) => true
    case _ => false
  }

  def makeAbsolute(url: String): String = url match {
    case matchUrl if matchUrl.startsWith(startUrl.toString) => matchUrl
    case matchUrl if matchUrl.startsWith("/") => startUrl.toString + matchUrl
  }

  def cleanLink(url: String): String = url match {
    case matchUrl if matchUrl.endsWith("/") => cleanLink(url.dropRight(1))
    case matchUrl if matchUrl.contains("#") => cleanLink(matchUrl.split("#")(0))
    case _ => url
  }


  def saveToFile(): Unit = {
    implicit val formats = DefaultFormats
    val json = write(resourcesObject)
    // FileWriter
    val file = new File(s"${startUrl.getHost}.json")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(json)
    bw.close()
  }


}
