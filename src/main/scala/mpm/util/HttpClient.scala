package mpm.util

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.{HttpExt, Http}
import akka.stream.ActorMaterializer

import scala.concurrent.Future


/**
  * Created by Michael on 25/07/2016.
  */
//We don't need tests for it as it's just a thing layer to allow testing
class HttpClient()(implicit system: ActorSystem, materializer: ActorMaterializer){

  def http: HttpExt = Http()

  def sendRequest(httpRequest: HttpRequest): Future[HttpResponse] =
    http.singleRequest(httpRequest)

}
