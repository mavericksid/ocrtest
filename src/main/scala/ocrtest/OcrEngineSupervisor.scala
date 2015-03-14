package ocrtest

import akka.actor.Actor
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy
import akka.actor.Props

class OcrEngineSupervisor extends Actor {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => Resume
    }

  def receive: Receive = {
    case (props: Props, name: String) => sender ! context.actorOf(props, name)
  }
}