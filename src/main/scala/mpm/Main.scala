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
    val system = ActorSystem("Crawler")
    val crawler = system.actorOf(Props[Crawler], "crawler")
    crawler ! Start(new URL(args.head))
  }

}
