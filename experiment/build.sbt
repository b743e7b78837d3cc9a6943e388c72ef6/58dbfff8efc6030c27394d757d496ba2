name := "experiment"

version := "1.0"

scalaVersion := "2.12.3"
// scalaVersion := "2.11.11"

scalacOptions += "-feature"


cancelable in Global := true // Terminate a run in sbt without terminating sbt
fork in run := true          // seperate jvm for app and sbt
connectInput in run := true  // forward stdin from sbt shell to app

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.9" % Test,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9",
  "com.typesafe.akka" %% "akka-actor" % "2.5.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % "test",
  "com.typesafe.akka" %% "akka-stream" % "2.5.4",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.4" % Test
)
libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.3"
//  "org.json4s" %% "json4s-native" % "3.2.10"
)
libraryDependencies ++= Seq(
//  "org.postgresql" % "postgresql" % "42.1.4",
//  "org.scalikejdbc" % "scalikejdbc_2.10" % "3.0.2", // Should work with scala 2.12 but doesnt
//  "com.h2database" % "h2" % "1.4.196",
// "ch.qos.logback" % "logback-classic" % "1.2.3",
//  "org.apache.commons" % "commons-dbcp2" % "2.1.1"
)
libraryDependencies ++= Seq (
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "org.apache.commons" % "commons-dbcp2" % "2.0.1"
)
libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.13.1"
)
libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
libraryDependencies += "ch.megard" % "akka-http-cors_2.12" % "0.2.1"

scalacOptions += "-deprecation"


