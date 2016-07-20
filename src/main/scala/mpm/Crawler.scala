package mpm

import java.net.URL
import java.util.concurrent.Executors

import akka.actor.{ActorRefFactory, Actor}
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.jsoup.Jsoup
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import org.jsoup.nodes.{Element, Document}
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConversions._
/**
  * Created by Michael on 20/07/2016.
  */
class Crawler extends Actor{

  def actorRefFactory: ActorRefFactory = context
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(200))



  //val crawled = scala.collection.mutable.SortedSet[URL]()

  def receive: Receive = {
    case Start(url) => start(url)
  }

  def start(url: URL): Unit = {

    getLinks(url)

  }

  protected def getLinks(url: URL) = {//: Future[List[URL]] = {

    //val response = Jsoup.connect()

    val res = Http().singleRequest(HttpRequest(uri = url.toString))//todo (mpm) follow redirects

    res.map( httpResponse => {
      val futureBody = Unmarshal(httpResponse.entity).to[String]
      futureBody.map{ body =>
        val doc = Jsoup.parse(body)

        val elements = doc.select("a").toList

        elements.map( el => println(el.attr("href")))



      }
    })

  }


}
