package com.ocrtest.actors

import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy.Resume
import akka.actor.actorRef2Scala

/**
 * OCR Engine supervisor resumes the actor operations
 * on any kind of exception
 */
class OcrEngineSupervisor extends Actor {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => Resume
    }

  def receive: Receive = {
    case (props: Props, name: String) => sender ! context.actorOf(props, name)
  }
}
