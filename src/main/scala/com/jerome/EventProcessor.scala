package com.jerome

import cats.effect.{IO, Resource}
import scala.io.{BufferedSource, Source}
import io.circe._

class EventProcessor {

  def processFile(): Unit = {
    val eventsFile: IO[BufferedSource] = IO(Source.fromFile("events"))
    Resource
      .fromAutoCloseable(eventsFile)
      .use(buffer => IO(processEventLogLines(buffer.getLines())))
      .unsafeRunSync()
  }

  def processEventLogLines(eventLogLines: Iterator[String]): Unit = {
    eventLogLines.foldLeft(Map.empty[String, EventLog]) { (eventLogMap, eventLogLine) =>
      toEventLog(eventLogLine) match {
        case Right(eventLog) => processEventLog(eventLogMap, eventLog)
        case Left(errorMessage) =>
          println(errorMessage)
          eventLogMap
      }
    }
  }

  def processEventLog(eventLogsMap: Map[String, EventLog],  eventLog: EventLog): Map[String, EventLog] = {
    if (eventLogsMap.contains(eventLog.id)) {
      verifyAlert(eventLogsMap(eventLog.id), eventLog)
      eventLogsMap - eventLog.id
    }
    else
      eventLogsMap + (eventLog.id -> eventLog)
  }

  def verifyAlert(firstEventLog: EventLog, lastEventLog: EventLog): Unit = {
    if (EventLog.State.Finished == lastEventLog.state) {
      val duration = lastEventLog.timestamp - firstEventLog.timestamp
      if (duration > 4) {
        println(s"Alert for Event with id: ${firstEventLog.id} duration: $duration")
      }
    } else {
      val duration = firstEventLog.timestamp - lastEventLog.timestamp
      if (duration > 4) {
        println(s"Alert for Event with id: ${firstEventLog.id} duration: $duration")
      }
    }
  }

  def toEventLog(eventLogLine: String): Either[String, EventLog] = {
    parser.decode[EventLog](eventLogLine).left
      .map(_ => s"There was an error parsing the following event log line: $eventLogLine")
  }

}
