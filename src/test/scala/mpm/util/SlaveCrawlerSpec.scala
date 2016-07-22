package mpm.util

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor._
import akka.testkit.TestProbe
/**
  * Created by Michael on 22/07/2016.
  */
class SlaveCrawlerSpec extends SpecificationLike with TestKit(ActorSystem()){

  private val uasClient = TestProbe()

}
