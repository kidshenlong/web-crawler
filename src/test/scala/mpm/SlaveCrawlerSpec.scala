package mpm

import java.net.URL

import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.stream.ActorMaterializer
import akka.testkit.{TestActorRef, TestProbe, TestKit}
import mpm.Domain.Resource
import mpm.util.HttpClient
import org.jsoup.Jsoup
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import org.specs2.mock.Mockito
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.actor._
import akka.http.scaladsl.{HttpExt, Http}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Michael on 22/07/2016.
  */
class SlaveCrawlerSpec extends Specification with BeforeAfterAll with Mockito {

  sequential

  implicit val system = ActorSystem("test-slave-crawler")
  implicit val materializer = ActorMaterializer()

  var slaveCrawler: SlaveCrawler = null
  var mockedSelfActorRef: TestProbe = null
  var mockedMasterCrawlerActorRef: TestProbe = null

  val domain = new URL("http://test.com")

  val link1 = "http://test.com/page/1"
  val link1AsURL = new URL(link1)
  val link2 = "http://test.com/page/2"
  val link2AsURL = new URL(link2)
  val link3 = "http://test.com/page/3"
  val link3AsURL = new URL(link3)

  val asset1 = "http://test.com/test.img"
  val asset2 = "http://test.com/jquery.js"
  val asset3 = "http://test.com/styles.css"

  val link1Body = "<html><head></head><body>" +
    s"<a href='$link2'></a><img src='$asset1'>" +
    s"<script src='$asset2'></script>" +
    s"<link rel='stylesheet' href='$asset3'>" +
    "</body></html>"

  val link1Document = Jsoup.parse(link1Body)


  def setUp() = {
    mockedSelfActorRef = TestProbe()
    mockedMasterCrawlerActorRef = TestProbe()

    val httpClient = mock[HttpClient]
    val httpEntity = HttpEntity(link1Body)
    httpClient.sendRequest(any) returns Future(HttpResponse(entity = httpEntity))

    val actorRef = TestActorRef(new SlaveCrawler(mockedMasterCrawlerActorRef.ref, domain, httpClient))
    slaveCrawler = actorRef.underlyingActor

    slaveCrawler.selfActorRef = mockedSelfActorRef.ref

  }


  override def beforeAll(): Unit = {
    setUp()
  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Slave Crawler" should {
    "handleCrawl should send all underlying messages and return a Crawl Complete message to master" in {
      slaveCrawler.handleCrawl(link1AsURL)

      mockedMasterCrawlerActorRef.expectMsg(CrawlComplete(Resource(link1, Set(link2), Set(asset1, asset2, asset3))))

      "" == ""
    }

    "test" in {
      Await.result(slaveCrawler.handleGetUrlBody(link1AsURL), 2 seconds) == link1Body
    }

    "test 2 " in {
      Await.result(slaveCrawler.handleParseBody(link1Body), 2 seconds).body().toString === link1Document.body().toString
    }

    "test 3 " in {
      Await.result(slaveCrawler.handleExtractLinks(link1Document), 2 seconds) == Set(link2)
    }

    "test 4 " in {
      Await.result(slaveCrawler.handleExtractStaticAssets(link1Document), 2 seconds) == Set(asset1, asset2, asset3)
    }
  }

}
