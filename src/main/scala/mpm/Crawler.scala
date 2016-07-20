package mpm

import java.net.URL

import akka.actor.Actor
import org.jsoup.Jsoup

/**
  * Created by Michael on 20/07/2016.
  */
class Crawler extends Actor{

  val crawled = scala.collection.mutable.SortedSet[URL]()

  def receive: Receive = {
    case Start(url) => start(url)
  }

  def start(url: URL): Unit = {

  }

  protected def getLinks(url: URL): List[URL] = {

  }


}
