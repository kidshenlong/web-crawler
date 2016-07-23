package mpm

import java.io.{FileWriter, BufferedWriter, File}
import java.net.URL
import java.util.concurrent.Executors

import akka.actor._
import akka.routing.{DefaultResizer, Broadcast, RoundRobinPool, SmallestMailboxPool}
import akka.stream.ActorMaterializer
import mpm.Domain.Resource
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._
import scala.concurrent.ExecutionContext
import scala.collection._
import scala.concurrent.duration._

//import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by Michael on 21/07/2016.
  */
class MasterCrawler(domain: URL) extends Actor {

  def actorRefFactory: ActorRefFactory = context
  implicit val system = context.system
  //Cached Thread Pool for many short lived tasks
  implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  context.system.eventStream.subscribe(self, classOf[DeadLetter])


  //In a real world situation this would be a call to a strongly consistent Database
  //or something function ie: Memoisation
  var pendingURLs: mutable.Set[URL] = mutable.Set(domain)
  var completedUrls: mutable.Set[URL] = mutable.Set.empty[URL]
  var resources: mutable.Set[Resource] = mutable.Set.empty[Resource]

  val resizer = DefaultResizer(lowerBound = 10, upperBound = 40)
  var slaveRouter: ActorRef = context.actorOf(SmallestMailboxPool(10, Some(resizer)).props(Props(classOf[SlaveCrawler], self, domain)))

  override def preStart(): Unit = {
    //self ! Start()
  }

  def receive: Receive = {
    //case Start() => self ! WorkAvailable()
    case GiveWork() => handleCrawl(sender())
    case CrawlComplete(resource) => handleCrawlComplete(resource)
    case WorkAvailable() => slaveRouter ! Broadcast(WorkAvailable())
    case Finish() => context.stop(self)
    case DeadLetter(msg, from, to) => println(msg.toString.take(20) + s" $from --- $to")

  }

  def handleCrawl(worker: ActorRef) = {
    if(pendingURLs.nonEmpty){
      //Reset Timeout Timer
      val url = pendingURLs.head
      pendingURLs = pendingURLs.filterNot(_ == url)
      worker ! Crawl(url)
    } else {
      //Start Timeout Timer
      //self ! PoisonPill//Finish()
      //context.stop(self)
    }
  }

  def handleCrawlComplete(resource: Resource) = {
    println(resource)
    completedUrls += new URL(resource.path)
    resources += resource
    pendingURLs = pendingURLs ++ resource.links.map(new URL(_)).filter(li => if(completedUrls.contains(li)) false else true)
    self ! WorkAvailable()
  }

}
