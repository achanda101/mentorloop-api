package com.fikafi.mentorloop.api

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.fikafi.mentorloop.persistence.MongoConnector

object MentorloopServer extends App with MentorRoutes {

  implicit val system: ActorSystem = ActorSystem("mentorloop-actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  //TODO: Add a level of supervision here. One actor to supervise registry for each repo.
  val userRegistryActor: ActorRef = system.actorOf(
    UserRegistryActor.props(MongoConnector.connection),
    "userRegistryActor")

  val internshipRegistryActor: ActorRef = system.actorOf(
    InternshipRegistryActor.props(MongoConnector.connection),
    "internshipRegistryActor")

  lazy val routes: Route = userRoutes

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}