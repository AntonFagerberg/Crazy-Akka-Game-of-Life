package scala

import akka.actor.{ActorRef, Actor}
import akka.pattern.ask
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._

class Checker(neighbours: Seq[ActorRef]) extends Actor {
  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case "check" => sender ! {
      neighbours.map { cell =>
        cell ? "alive"
      }.map { answer =>
        Await.result(answer, timeout.duration).asInstanceOf[Boolean]
      }.count(_ == true)
    }
  }
}
