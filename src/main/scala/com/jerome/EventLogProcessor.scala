package com.jerome

import com.jerome.EventLog._
import cats.effect.IO
import cats.syntax.applicative._
import io.scalaland.chimney.dsl._

object EventLogProcessor {

  def processEventLogLines(eventLogLines: Iterator[String]): IO[Unit] = {
    for {
      _ <- EventAlertDao.initTable

      _ <- eventLogLines.foldLeft(Map.empty[String, EventLog].pure[IO]) {
        (registeredEventLogMap, eventLogLine) =>
          eventLogLine
            .toEventLog
            .map { processEventLog(registeredEventLogMap, _) }
            .getOrElse(registeredEventLogMap)
      }
    } yield ()
  }

  private def processEventLog(registeredEventLogMapIO: IO[Map[String, EventLog]],
                              eventLog: EventLog): IO[Map[String, EventLog]] = {
    registeredEventLogMapIO.flatMap { registeredEventLogMap =>
      if (registeredEventLogMap.contains(eventLog.id))
        for {
          _ <- saveEventAlert(registeredEventLogMap(eventLog.id), eventLog)
        } yield registeredEventLogMap - eventLog.id
      else
        (registeredEventLogMap + (eventLog.id -> eventLog)).pure[IO]
    }
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
}
