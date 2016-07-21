package mpm

import java.net.URL

/**
  * Created by Michael on 20/07/2016.
  */
sealed abstract class Message()

case class Start()
case class Explore(url: URL)
case class Finish()
