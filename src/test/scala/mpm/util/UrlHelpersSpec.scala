package mpm.util

import java.net.URL

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
class UrlHelpersSpec extends Specification {

  val domain = "http://test.com"

  implicit val domainAsURL: URL = new URL(domain)

  new UrlHelper {

    "Helpers" should {
      "isInternalLink should return true for an internal link" in {
        isInternalLink("/link")
        isInternalLink("http://test.com/link")
      }
      "isInternalLink should return false for an external link" in {
        isInternalLink("http://not-test.com/link") === false
      }
      "makeAbsolute should return an absolute link" in {
        makeAbsolute("/link") == domain + "/link"
        makeAbsolute("http://test.com/link") == domain + "/link"
        makeAbsolute("link") == domain + "/link"
      }
      "cleanLink should return a clean link" in {
        cleanLink("http://test.com/link/") == "http://test.com/link"
        cleanLink("http://test.com/link#mid-point") == "http://test.com/link"
      }
    }

  }
}

