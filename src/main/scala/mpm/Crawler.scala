package mpm

import java.net.URL
import java.util.concurrent.Executors

import akka.actor.{Actor, ActorRefFactory}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
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


  def receive: Receive = {
    case Start() => start()
    case Explore(url) => explore(url)
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
      if(queue == 0) println("Finished!!")
    }
  }


  protected def scrapeUrl(url: URL): Future[List[URL]] = {
    println(s"[info] Scraping ${url.toString}")

    crawled += url

    queue = queue + 1


    val res = Http().singleRequest(HttpRequest(uri = url.toString))//todo (mpm) follow redirects

    res.flatMap{ httpResponse =>
      val futureBody = Unmarshal(httpResponse.entity).to[String]
      futureBody.map{ body =>

        println(s"[info] Scraping ${url.toString} complete!")

        queue = queue - 1

        val doc = Jsoup.parse(body)

        val elements = doc.select("a").toList
        val links = elements.map(_.attr("href"))

        val filteredLinks = links.filter(isInternalLink)
          .map(makeAbsolute)
          .map(cleanLink)
          .map( l => new URL(l))
          .filter(li => if(crawled.contains(li)) false else true)

        filteredLinks.foreach(link => println(s"[info] Found ${link.toString}"))

        filteredLinks
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


}
