lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.12"
lazy val reactiveMongoVersion = "0.13.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization         := "com.fikafi",
      scalaVersion         := "2.12.5"
    )),
    name := "mentor-loop",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test,

      "org.reactivemongo" %% "reactivemongo"        % reactiveMongoVersion,
      "org.reactivemongo" %% "reactivemongo-akkastream" % "0.13.0",

      "com.typesafe.akka" %% "akka-slf4j" % "2.5.13",
      "ch.qos.logback" % "logback-classic" % "0.9.28"
    )
  )
