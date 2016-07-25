package mpm

import java.net.URL

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestActorRef, TestProbe}
import mpm.Domain.Resource
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by Michael on 22/07/2016.
  */
class MasterCrawlerSpec extends Specification with BeforeAfterAll {

  sequential

  implicit val system = ActorSystem("test-master-crawler")

  //fixtures
  val domain = new URL("http://test.com")

  val link1 = "http://test.com/page/1"
  val link1AsURL = new URL(link1)
  val link2 = "http://test.com/page/2"
  val link2AsURL = new URL(link2)
  val link3 = "http://test.com/page/3"
  val link3AsURL = new URL(link3)

  val asset = "http://test.com/test.img"

  val foundResource = Resource(link1, Set(link2, link3), Set(asset))


  val actorRef = TestActorRef(new MasterCrawler(domain){
    override def saveToFile(resources: mutable.Set[Resource], domain: URL): Future[Unit] = {
      println("call saveToFile")
      Future(Unit)
    }
  })

  var masterCrawler: MasterCrawler = null
  var mockedSelfActorRef: TestProbe = null
  var mockedSlaveRouter: TestProbe = null

  def setUp() = {

    mockedSelfActorRef = TestProbe()
    mockedSlaveRouter = TestProbe()

    val actorRef = TestActorRef(new MasterCrawler(domain))
    masterCrawler = actorRef.underlyingActor

    masterCrawler.selfActorRef = mockedSelfActorRef.ref
    masterCrawler.slaveRouter =  mockedSlaveRouter.ref

  }

  override def beforeAll(): Unit = {
    setUp()
  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Master Crawler" should {
    "handleCrawl should send a message to the slave router and add URL to pending and start queues" in {

      masterCrawler.handleCrawl()

      masterCrawler.pendingURLs.isEmpty
      masterCrawler.startedURLs.isEmpty

      mockedSlaveRouter.expectMsg(Crawl(domain))

      masterCrawler.pendingURLs.size == 1
      masterCrawler.startedURLs.size == 1

      "" === ""
    }
  }

  "handleCrawl will not send a message to the slave router if there are no URLs in the pending queue" in {
    masterCrawler.handleCrawl()

    masterCrawler.pendingURLs.empty

    mockedSlaveRouter.expectNoMsg()

    "" === ""
  }

  "handleCrawl will not send a message to the slave router if the URL is already in started queue" in {
    masterCrawler.handleCrawl()

    masterCrawler.startedURLs = masterCrawler.pendingURLs

    mockedSlaveRouter.expectNoMsg()

    "" === ""
  }

  "handleCrawl will send a finish to self if pending and started queues are both empty" in {
    masterCrawler.handleCrawl()

    masterCrawler.startedURLs.empty
    masterCrawler.pendingURLs.empty

    mockedSelfActorRef.expectMsg(Finish(mutable.Set()))


    "" === ""
  }

  "handleCrawlComplete will send a Work Available Message and update queues " in {

    masterCrawler.startedURLs += link1AsURL
    masterCrawler.pendingURLs.empty
    masterCrawler.finishedUrls.empty

    masterCrawler.handleCrawlComplete(foundResource)

    mockedSelfActorRef.expectMsg(WorkAvailable)

    masterCrawler.startedURLs.empty
    masterCrawler.pendingURLs.contains(link2AsURL)
    masterCrawler.pendingURLs.contains(link3AsURL)
    masterCrawler.finishedUrls.contains(link1AsURL)

    "" === ""
  }

  "handleFinish will rturn a Future Unit" in {
    masterCrawler.handleFinish(mutable.Set(foundResource))

    "" === ""
  }

}
