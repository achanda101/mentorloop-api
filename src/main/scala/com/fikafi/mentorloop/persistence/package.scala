package com.fikafi.mentorloop

import reactivemongo.api.MongoDriver
import reactivemongo.core.nodeset.Authenticate

package object persistence {

  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()

  val mongoUriList = List(config.getConfig("fikafi.db.mongo").getString("uri"))
  val dbname = config.getConfig("fikafi.db.mongo").getString("dbname")
  val mongoDbUsername = config.getConfig("fikafi.db.mongo").getString("username")
  val mongoDbPassword = config.getConfig("fikafi.db.mongo").getString("password")
}
