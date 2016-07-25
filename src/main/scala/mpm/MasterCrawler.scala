package mpm

import java.io.{FileWriter, BufferedWriter, File}
import java.net.URL
import java.util.concurrent.Executors

import akka.actor._
import akka.routing.{DefaultResizer, Broadcast, RoundRobinPool, SmallestMailboxPool}
import akka.stream.ActorMaterializer
import mpm.Domain.Resource
import mpm.util.{HttpClient, FileHelper}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._
import scala.concurrent.{Future, ExecutionContext, blocking}
import scala.collection._
import scala.concurrent.duration._
import language.postfixOps
//import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by Michael on 21/07/2016.
  */
class MasterCrawler(domain: URL) extends Actor{

  //def actorRefFactory: ActorRefFactory = context
  implicit val system = context.system
  //Cached Thread Pool for many short lived tasks
  implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  implicit val materializer = ActorMaterializer()

  var file = new FileHelper()


  //In a real world situation this would be a call to a strongly consistent Database
  //or something function ie: Memoisation
  var pendingURLs: mutable.Set[URL] = mutable.Set(domain)
  var startedURLs: mutable.Set[URL] = mutable.Set.empty[URL]
  var finishedUrls: mutable.Set[URL] = mutable.Set.empty[URL]
  var resources: mutable.Set[Resource] = mutable.Set.empty[Resource]

  val resizer = None//Some(DefaultResizer(lowerBound = 10, upperBound = 40))
  var slaveRouter: ActorRef = context.actorOf(SmallestMailboxPool(40, resizer).props(SlaveCrawler.props(self, domain, new HttpClient())))

  var selfActorRef = self

  def receive: Receive = {
    case Start => handleCrawl()
    case CrawlComplete(resource) => handleCrawlComplete(resource)
    case WorkAvailable => handleCrawl()
    case Finish(resourcesFound) => handleFinish(resourcesFound)
  }

  def handleCrawl() = {
    if(pendingURLs.nonEmpty){
      pendingURLs.foreach { url =>
        pendingURLs = pendingURLs.filterNot(_ == url)

        if(!startedURLs.contains(url)) {
          slaveRouter ! Crawl(url)
          startedURLs += url
        }
      }
    } else if(pendingURLs.isEmpty && startedURLs.isEmpty){
      selfActorRef ! Finish(resources)
    }
  }

  def handleCrawlComplete(resource: Resource) = {
    finishedUrls += new URL(resource.path)
    resources += resource
    startedURLs -= new URL(resource.path)

    val newUrls = resource.links.map(new URL(_)).filter(li => if(finishedUrls.contains(li)) false else true)

    if(newUrls.nonEmpty){
      pendingURLs = pendingURLs ++ newUrls
    }

    selfActorRef ! WorkAvailable
  }

  def handleFinish(resourcesFound: mutable.Set[Resource]): Unit = {
    file.saveToFile(resourcesFound, domain).map { _ =>
      println(s"[info] File created: ${domain.getHost}.json")
      context.stop(selfActorRef)
    }
  }

}
