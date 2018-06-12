package com.fikafi.mentorloop.persistence

import akka.actor.{ Actor, ActorLogging, Props }
import reactivemongo.api.BSONSerializationPack.Document
import reactivemongo.api.{ DefaultDB, MongoConnection, MongoDriver }
import reactivemongo.core.nodeset.Authenticate

import scala.concurrent.Future
import scala.util.Try

object MongoConnector {
  val mongoDriver = new MongoDriver
  val credentials = List(Authenticate(dbname, mongoDbUsername, mongoDbPassword))

  val connection = mongoDriver.connection(mongoUriList, authentications = credentials)
}
