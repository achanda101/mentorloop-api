package com.fikafi.mentorloop.api

import com.fikafi.mentorloop.api.UserRegistryActor.{ User }
import com.fikafi.mentorloop.api.InternshipRegistryActor.{ Internship }

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat4(User)
  implicit val internshipJsonFormat = jsonFormat6(Internship)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}