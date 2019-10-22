package com.jerome

import io.circe._
import cats.instances.either._
import cats.instances.string._
import cats.syntax.apply._
import cats.syntax.eq._

final case class EventLog(id: String,
                    state: EventLog.State,
                    timestamp: Long,
                    host: Option[String],
                    logType: Option[String])

object EventLog {

  sealed trait State extends Product with Serializable

  object State {
    case object Started extends State
    case object Finished extends State

    def fromString(state: String): State =
      if ("STARTED" === state.toUpperCase)
        Started
      else
        Finished
  }

  implicit val eventLogDecoder: Decoder[EventLog] = (hCursor: HCursor) => (
      hCursor.get[String]("id"),
      hCursor.get[String]("state").map(State.fromString),
      hCursor.get[Long]("timestamp"),
      hCursor.getOrElse[Option[String]]("host")(None),
      hCursor.getOrElse[Option[String]]("type")(None)
    ).mapN(EventLog.apply)

  implicit class EventLogJsonParser(eventLogJson: String) {
    def toEventLog: Either[Error, EventLog] =
      parser.decode[EventLog](eventLogJson)
  }

}

