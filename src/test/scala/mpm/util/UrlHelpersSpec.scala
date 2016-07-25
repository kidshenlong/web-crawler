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
class UrlHelpersSpec extends Specification{

  val linkFixture = """<a href="http://random.com"><img src="http://random.com/lolcatz.jpg">"""

  val linkDocumentFixture = Jsoup.parse(linkFixture)

  new UrlHelper {

    "Helpers" should {
        "" in {
          "" === ""
        }
      //isInternalLink

      //makeAbsolute

      //cleanLink
    }

  }


}
