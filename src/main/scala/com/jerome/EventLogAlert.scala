package com.jerome

case class EventLogAlert(id: String,
                         duration: Int,
                         logType: Option[String],
                         host: Option[String],
                         isAlert: Boolean)
