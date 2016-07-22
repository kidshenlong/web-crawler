package mpm

import java.net.URL

import akka.actor.{ActorSystem, Props}

/**
  * Created by Michael on 20/07/2016.
  */
object Main {

  def main(args: Array[String]): Unit = {
    val domain = new URL(clean(args.head))

    val system = ActorSystem("Crawler")

    val crawler = system.actorOf(Props(new MasterCrawler(domain)), "crawler")

    crawler ! Idle()
  }

  def clean(url: String) = url match{
    case matchUrl if matchUrl.endsWith("/") => matchUrl.dropRight(1)
    case _ => url
  }
}


