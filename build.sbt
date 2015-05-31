name := "akka-toy"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.9",
    "com.ning" % "async-http-client" % "1.7.19",
    "org.jsoup" % "jsoup" % "1.8.1"
  )
}
