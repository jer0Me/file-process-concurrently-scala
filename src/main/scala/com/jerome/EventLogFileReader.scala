package com.jerome

import cats.effect.{IO, Resource}

import scala.io.{BufferedSource, Source}

object EventLogFileReader {

  def process(fileName: String): Unit = {
    val eventsFile: IO[BufferedSource] = IO(Source.fromFile(fileName))
    Resource
      .fromAutoCloseable(eventsFile)
      .use(buffer => EventLogProcessor.processEventLogLines(buffer.getLines()))
      .unsafeRunSync
  }

}
