package com.fikafi.mentorloop.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.common.{ EntityStreamingSupport, JsonEntityStreamingSupport }

import scala.concurrent.duration._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.Timeout
import reactivemongo.akkastream.State

import com.fikafi.mentorloop.api.UserRegistryActor._
import com.fikafi.mentorloop.api.InternshipRegistryActor._

trait MentorRoutes extends JsonSupport {

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[MentorRoutes])

  // other dependencies that MentorRoutes use
  def userRegistryActor: ActorRef
  def internshipRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  val userRoutes: Route =
    pathPrefix("v0" / "users") {
      concat(
        pathEndOrSingleSlash {
          concat(
            get {
              val users: Future[Source[User, Future[State]]] =
                (userRegistryActor ? GetUsers()).mapTo[Source[User, Future[State]]]
              complete(users)
            },
            post {
              entity(as[User]) { user =>
                val userCreated: Future[ActionPerformed] =
                  (userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]
                onSuccess(userCreated) { performed: ActionPerformed =>
                  log.info("Created user [{}]: {}", user.name, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        path(Segment) { id =>
          concat(
            get {
              val maybeUser: Future[Option[User]] =
                (userRegistryActor ? GetUser(id)).mapTo[Option[User]]
              rejectEmptyResponse {
                complete(maybeUser)
              }
            },
            put {
              entity(as[User]) { user =>
                onSuccess((userRegistryActor ? UpdateUser(id, user)).mapTo[ActionPerformed]) { p: ActionPerformed =>
                  complete((StatusCodes.Created, p))
                }
              }
            },
            delete {
              val userDeleted: Future[ActionPerformed] =
                (userRegistryActor ? DeleteUser(id)).mapTo[ActionPerformed]
              onSuccess(userDeleted) { performed =>
                log.info("Deleted user [{}]: {}", id, performed.description)
                complete((StatusCodes.OK, performed))
              }
            })
        })
    } ~
      pathPrefix("v0" / "internship") {
        concat(
          pathEndOrSingleSlash {
            concat(
              get {
                val internships: Future[Source[User, Future[State]]] =
                  (internshipRegistryActor ? GetInternships()).mapTo[Source[User, Future[State]]]
                complete(internships)
              },
              post {
                entity(as[Internship]) { in =>
                  val userCreated: Future[ActionPerformed] =
                    (internshipRegistryActor ? CreateInternship(in)).mapTo[ActionPerformed]
                  onSuccess(userCreated) { performed: ActionPerformed =>
                    log.info("{}", performed.description)
                    complete((StatusCodes.Created, performed))
                  }
                }
              })
          },
          path(Segment) { id =>
            concat(
              get {
                val maybeUser: Future[Option[Internship]] =
                  (internshipRegistryActor ? GetInternship(id)).mapTo[Option[Internship]]
                rejectEmptyResponse {
                  complete(maybeUser)
                }
              },
              put {
                entity(as[Internship]) { in =>
                  onSuccess((internshipRegistryActor ? UpdateInternship(id, in)).mapTo[ActionPerformed]) { p: ActionPerformed =>
                    complete((StatusCodes.Created, p))
                  }
                }
              },
              delete {
                val internshipDeleted: Future[ActionPerformed] =
                  (internshipRegistryActor ? DeleteInternship(id)).mapTo[ActionPerformed]
                onSuccess(internshipDeleted) { performed =>
                  log.info("{}", performed.description)
                  complete((StatusCodes.OK, performed))
                }
              })
          })
      }
}
