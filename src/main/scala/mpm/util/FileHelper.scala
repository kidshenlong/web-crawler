package mpm.util

import java.io.{BufferedWriter, File, FileWriter}
import java.net.URL

import mpm.Domain.Resource
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
/**
  * Created by Michael on 24/07/2016.
  */
class FileHelper {

  def saveToFile(resources: mutable.Set[Resource], domain: URL): Future[Unit] = Future { blocking {
    implicit val formats = DefaultFormats
    val json = write(resources)
    // FileWriter
    val file = new File(s"${domain.getHost}.json")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(json)
    bw.close()
  }}

}
