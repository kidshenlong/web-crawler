package mpm

import java.net.URL

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorLogging

/**
  * Created by Michael on 20/07/2016.
  */
object Main {

  def main(args: Array[String]): Unit = {
    val domain = new URL(clean(args.head))


    val system = ActorSystem("Crawler")

    val crawler = system.actorOf(Props(new Crawler(domain)), "crawler")

    crawler ! Start()
  }

  def clean(url: String) = url match{
    case matchUrl if matchUrl.endsWith("/") => matchUrl.dropRight(1)
    case _ => url
  }

}


