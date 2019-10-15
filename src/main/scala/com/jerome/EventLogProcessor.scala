package com.jerome

import cats.effect.IO
import com.typesafe.scalalogging.Logger
import io.circe._
import io.scalaland.chimney.dsl._
import org.slf4j.LoggerFactory

object EventLogProcessor {

  private val logger = Logger(LoggerFactory.getLogger("EventLogProcessor"))

  def processEventLogLines(eventLogLines: Iterator[String]): IO[Unit] = {
    for {
      _ <- EventAlertDao.initTable

      _ <- eventLogLines.foldLeft(IO(Map.empty[String, EventLog])) {
        (registeredEventLogMap, eventLogLine) =>
          parseEventLog(eventLogLine) match {
            case Right(eventLog) => processEventLog(registeredEventLogMap, eventLog)
            case _ => registeredEventLogMap
          }
      }
    } yield ()
  }

  private def processEventLog(registeredEventLogMap: IO[Map[String, EventLog]], eventLog: EventLog): IO[Map[String, EventLog]] = {
    registeredEventLogMap.flatMap(map => {
      if (map.contains(eventLog.id)) {
        for {
          _ <- saveEventAlert(map(eventLog.id), eventLog)
        } yield map - eventLog.id
      }
      else
        IO(map + (eventLog.id -> eventLog))
    })
  }

  private def saveEventAlert(firstEventLog: EventLog, lastEventLog: EventLog): IO[Unit] = {
    val duration: Int = (firstEventLog.timestamp - lastEventLog.timestamp).abs.toInt
    val eventLogAlert: EventLogAlert =
      firstEventLog.into[EventLogAlert]
        .withFieldComputed(_.duration, _ => duration)
        .withFieldComputed(_.isAlert, _ => duration > 4)
        .transform

    EventAlertDao.saveEventAlert(eventLogAlert)
  }

  private def parseEventLog(eventLogLine: String): Either[Error, EventLog] = {
    parser.decode[EventLog](eventLogLine) match {
      case Right(eventLog) => Right(eventLog)
      case Left(error) =>
        logger.error(s"There was an error parsing: $eventLogLine", error)
        Left(error)
    }
  }
}
