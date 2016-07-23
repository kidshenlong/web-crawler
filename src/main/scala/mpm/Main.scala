package mpm

import java.net.URL

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Michael on 20/07/2016.
  */
object Main {

  def main(args: Array[String]): Unit = {
    val domain = new URL(clean(args.head))

    val system = ActorSystem("Crawler")

    val crawler = system.actorOf(Props(new MasterCrawler(domain)), "crawler")

    system.actorOf(Props(classOf[Terminator], crawler), "terminator")

  }

  def clean(url: String) = url match{
    case matchUrl if matchUrl.endsWith("/") => matchUrl.dropRight(1)
    case _ => url
  }

  class Terminator(ref: ActorRef) extends Actor with ActorLogging {
    context watch ref
    def receive = {
      case Terminated(_) =>
        log.info("{} has terminated, shutting down system", ref.path)
        context.system.terminate()
    }
  }



}


