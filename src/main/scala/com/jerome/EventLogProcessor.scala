package com.jerome

import cats.effect.IO
import com.typesafe.scalalogging.Logger
import io.circe._
import io.scalaland.chimney.dsl._
import org.slf4j.LoggerFactory

object EventLogProcessor {

  val logger = Logger(LoggerFactory.getLogger("EventLogProcessor"))

  def processEventLogLines(eventLogLines: Iterator[String]): IO[Unit] = {
    for {
      _ <- EventAlertDao.initTable

      - <- eventLogLines.foldLeft(IO(Map.empty[String, EventLog])) { (eventLogMap, eventLogLine) =>
             parseEventLog(eventLogLine) match {
               case Right(eventLog) => processEventLog(eventLogMap, eventLog)
               case _ => eventLogMap
             }
      }
    } yield ()
  }

  def processEventLog(eventLogsMap: IO[Map[String, EventLog]],  eventLog: EventLog): IO[Map[String, EventLog]] = {
    eventLogsMap.flatMap(map => {
        if (map.contains(eventLog.id)) {
          for {
            _ <- checkEventLogDuration(map(eventLog.id), eventLog)
          } yield map - eventLog.id
        }
        else
          IO(map + (eventLog.id -> eventLog))
    })
  }

  def checkEventLogDuration(firstEventLog: EventLog, lastEventLog: EventLog): IO[Int] = {
    val duration: Int = (firstEventLog.timestamp - lastEventLog.timestamp).abs.toInt
    val eventLogAlert: EventLogAlert =
      firstEventLog.into[EventLogAlert]
        .withFieldComputed(_.duration, _ => duration)
        .withFieldComputed(_.isAlert, _ => duration > 4)
        .transform

    EventAlertDao.saveEventAlert(eventLogAlert)
  }

  def parseEventLog(eventLogLine: String): Either[Error, EventLog] = {
    parser.decode[EventLog](eventLogLine) match {
      case Right(eventLog) => Right(eventLog)
      case Left(error) =>
        logger.error(s"There was an error parsing: $eventLogLine", error)
        Left(error)
    }
  }
}
