#Web Crawler

##To Build

1. install [sbt](http://www.scala-sbt.org/0.13/docs/Setup.html)
2. run `sbt assembly`

##To Run
With Built Jar run `java -jar target/scala-2.11/web-crawler-assembly-1.0.jar http://example.com`
Or With SBT run `sbt "run http://example.com"`

On completion this will a produce a domain.json with a sitemap.

Enjoy!