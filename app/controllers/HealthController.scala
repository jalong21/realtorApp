package controllers

import play.api.Logger
import play.api.mvc._
import services.RedisService

import javax.inject.Inject
import scala.util.{Failure, Success}


class HealthController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  RedisService.setValue("health", "check")

  val log = Logger(this.getClass.getName)

  def index = Action {
    _ => {
      log.warn("healthy")
      Ok("true")
    }
  }

  def redisCheck = Action {
    _ => RedisService.getValue("health") match {
      case Success(status: String) => Ok(status)
      case Failure(ex) => InternalServerError(s"Error Checking Status.  ${ex.getMessage}")
    }
  }
}