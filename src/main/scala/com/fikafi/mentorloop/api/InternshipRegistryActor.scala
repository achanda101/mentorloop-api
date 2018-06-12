package com.fikafi.mentorloop.api

import java.net.URL

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.pipe
import com.fikafi.mentorloop.persistence.dbname
import reactivemongo.api.{ DefaultDB, MongoConnection }
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{ BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros, document }
import reactivemongo.akkastream.cursorProducer

import scala.concurrent.Future

object InternshipRegistryActor {

  final case class Internship(
    _id: Option[String],
    tagLine: String,
    description: String,
    prerequisites: Seq[String],
    categoryId: String,
    tags: Seq[String])
  final case class Category(
    _id: Option[String],
    title: String,
    description: String,
    pic: Image)
  final case class Tag(
    _id: Option[String],
    name: String,
    pic: Image)
  final case class Image(
    _id: Option[String],
    src: URL,
    altText: String)

  final case class GetInternships(sel: BSONDocument = document())
  final case class CreateInternship(user: Internship)
  final case class GetInternship(id: String)
  final case class DeleteInternship(name: String)
  final case class UpdateInternship(id: String, user: Internship)

  implicit def internshipWriter: BSONDocumentWriter[Internship] = Macros.writer[Internship]
  implicit def internshipReader: BSONDocumentReader[Internship] = Macros.reader[Internship]

  def props(conn: MongoConnection): Props = Props(new InternshipRegistryActor(conn))

}

class InternshipRegistryActor(
  connection: MongoConnection) extends Actor with ActorLogging {
  import InternshipRegistryActor._
  import MentorloopServer._

  implicit val ec = context.dispatcher

  val db: Future[DefaultDB] = connection.database(dbname)

  def internshipCollection: Future[BSONCollection] = db.map(_.collection("internships"))

  def receive: Receive = {

    case GetInternships(sel: BSONDocument) =>
      internshipCollection.map(_.find(sel).cursor[Internship]().documentSource()) pipeTo sender()

    case CreateInternship(in) =>
      val id = Some(BSONObjectID.generate.stringify)
      val u: Internship = Internship(id, in.tagLine, in.description, in.prerequisites, in.categoryId, in.tags)
      internshipCollection.flatMap(_.insert(u)).map { r =>
        r.ok match {
          case true => ActionPerformed(s"Created: ${id}")
          case false => ActionPerformed(s"Failed ${r.writeErrors}")
        }
      } pipeTo sender()

    case GetInternship(id) =>
      val selector: BSONDocument = document("_id" -> id)
      internshipCollection.flatMap(_.find(selector).one[Internship]) pipeTo sender()

    case UpdateInternship(id, internship) =>
      internshipCollection.flatMap(_.update(document({ "_id" -> id }), internship)) pipeTo sender

    case DeleteInternship(id) =>
      internshipCollection.flatMap(_.remove(document({ "_id" -> id }))) pipeTo sender()
  }
}