package mpm.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.specs2.mutable.Specification
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

/**
  * Created by Michael on 22/07/2016.
  */
class HelpersSpec extends Specification{

  val linkFixture = """<a href="http://random.com"><img src="http://random.com/lolcatz.jpg">"""

  val linkDocumentFixture = Jsoup.parse(linkFixture)

  new Helpers {
    implicit val system = ActorSystem("test-actor-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()


    "Helpers" should {
      "extract a href from a html body" in {
        val expected = List("http://random.com")
        val actual = extractLinks(linkDocumentFixture)
        Await.result(actual, 2 seconds) === expected
      }

      "extract a static asset src from a html body" in {
        val expected = List("http://random.com/lolcatz.jpg")
        val actual = extractStaticAssets(linkDocumentFixture)
        Await.result(actual, 2 seconds) === expected
      }

      /*"parse html to a jsoup document" in {
        val expected = linkDocumentFixture
        val actual = parseHtml(linkFixture)
        Await.result(actual, 2 seconds) === expected
      }*/

      //isInternalLink

      //makeAbsolute

      //cleanLink
    }

  }


}
