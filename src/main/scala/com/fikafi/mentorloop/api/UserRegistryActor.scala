package com.fikafi.mentorloop.api

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.pipe
import com.fikafi.mentorloop.persistence.{ dbname }
import reactivemongo.api.{ DefaultDB, MongoConnection }
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{ BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros, document }
import reactivemongo.akkastream.{ cursorProducer }

import scala.concurrent.Future

object UserRegistryActor {

  final case class User(_id: Option[String], name: String, age: Int, country: String)

  final case class GetUsers(sel: BSONDocument = document())
  final case class CreateUser(user: User)
  final case class GetUser(id: String)
  final case class DeleteUser(name: String)
  final case class UpdateUser(id: String, user: User)

  implicit def userWriter: BSONDocumentWriter[User] = Macros.writer[User]
  implicit def userReader: BSONDocumentReader[User] = Macros.reader[User]

  def props(conn: MongoConnection): Props = Props(new UserRegistryActor(conn))

}

class UserRegistryActor(
  connection: MongoConnection) extends Actor with ActorLogging {
  import UserRegistryActor._
  import MentorloopServer._

  implicit val ec = context.dispatcher

  val db: Future[DefaultDB] = connection.database(dbname)

  def userCollection: Future[BSONCollection] = db.map(_.collection("users"))

  def receive: Receive = {

    case GetUsers(sel: BSONDocument) =>
      userCollection.map(_.find(sel).cursor[User]().documentSource()) pipeTo sender()

    case CreateUser(user) =>
      val id = Some(BSONObjectID.generate.stringify)
      val u: User = User(id, user.name, user.age, user.country)
      userCollection.flatMap(_.insert(u)).map { r =>
        r.ok match {
          case true => ActionPerformed(s"Created: ${id}")
          case false => ActionPerformed(s"Failed ${r.writeErrors}")
        }
      } pipeTo sender()

    case GetUser(id) =>
      val selector: BSONDocument = document("_id" -> id)
      userCollection.flatMap(_.find(selector).one[User]) pipeTo sender()

    case UpdateUser(id, user) =>
      userCollection.flatMap(_.update(document({ "_id" -> id }), user)) pipeTo sender

    case DeleteUser(id) =>
      userCollection.flatMap(_.remove(document({ "_id" -> id }))) pipeTo sender()
  }
}