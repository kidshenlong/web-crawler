package mpm

import java.io.{FileWriter, BufferedWriter, File}
import java.net.URL
import java.util.concurrent.Executors

import akka.actor._
import akka.routing.{RoundRobinPool, SmallestMailboxPool}
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
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(200))

  var task = system.scheduler.scheduleOnce(30 seconds, self, Finish())


  //In a real world situation this would be a call to a strongly consistent Database
  //or something function ie: Memoisation
  var pendingURLs: mutable.Set[URL] = mutable.Set(domain)
  var completedUrls: mutable.Set[URL] = mutable.Set.empty[URL]
  var resources: mutable.Set[Resource] = mutable.Set.empty[Resource]

  var slaveRouter: ActorRef = context.actorOf(SmallestMailboxPool(10).props(Props(classOf[SlaveCrawler], self, domain)))



  def receive: Receive = {
    case Start() =>
    case Idle() => handleIdle()
    case CrawlComplete(resource) => handleCrawlComplete(resource)
    case Finish() => handleFinish()
  }

  def handleIdle() = {
    if(pendingURLs.nonEmpty){
      val urls = pendingURLs.take(10).foreach{ url =>
        pendingURLs = pendingURLs.filterNot(_ == url)
        slaveRouter ! Crawl(url)
      }
    }
  }

  def handleCrawlComplete(resource: Resource) = {
    completedUrls += new URL(resource.path)
    resources += resource
    pendingURLs = pendingURLs ++ resource.links.map(new URL(_)).filter(li => if(completedUrls.contains(li)) false else true)
    handleIdle()
    task = system.scheduler.scheduleOnce(30 seconds, self, Finish())
  }

  def handleFinish() = {
    println("[info] Crawling Complete")
    saveToFile()
    System.exit(1)
  }

  def saveToFile(): Unit = {
    implicit val formats = DefaultFormats
    val json = write(resources)
    // FileWriter
    val file = new File(s"${domain.getHost}.json")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(json)
    bw.close()
  }


}
