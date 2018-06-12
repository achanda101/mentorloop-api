package com.fikafi.mentorloop.api

import akka.actor.AbstractActor.Receive
import akka.stream.scaladsl.Source
import com.fikafi.mentorloop.api.UserRegistryActor.{ CreateUser, GetUser, GetUsers, User }
import reactivemongo.akkastream.State
import reactivemongo.bson.{ BSONDocument, BSONObjectID, document }
import akka.pattern.pipe

import scala.concurrent.Future

trait EntityRegistry {

  final case class ActionPerformed(description: String)
  final case object GetMentors
  final case class GetMentor(id: String)
  final case class DeleteMentor(name: String)

  final case class GetEntity[T](id: String)

  def findAll[T]: Future[Source[User, Future[State]]]

  //  def receive: Receive = {
  //    case GetUsers =>
  //      findAll pipeTo sender()
  //    case CreateUser(userMsg) =>
  //      val id: String = BSONObjectID.generate.stringify
  //      val user: User = User(id, userMsg.name, userMsg.age, userMsg.country)
  //      create(user).map { r =>
  //        r.ok match {
  //          case true => ActionPerformed(s"Created ${id}")
  //          case false => ActionPerformed(s"Failed ${r.writeErrors}")
  //        }
  //      } pipeTo sender()
  //    case GetUser(id) =>
  //      val selector: BSONDocument = document("_id" -> id)
  //      find(selector) pipeTo sender()
  //    case GetEntity(id) =>
  //      val selector: BSONDocument = document("_id" -> id)
  //      findEntity(selector) pipeTo sender()
  //    //    case DeleteUser(id) =>
  //    //      mentors.find(_.id == id) foreach { user => mentors -= user }
  //    //      sender() ! ActionPerformed(s"User ${id} deleted.")
  //  }

}
