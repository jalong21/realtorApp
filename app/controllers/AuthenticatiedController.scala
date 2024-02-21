package controllers

import models.Role
import play.api.Logger
import play.api.mvc._
import services.AuthenticationService

abstract class AuthenticatiedController(controllerComponents: ControllerComponents) extends AbstractController(controllerComponents){

  val log = Logger(this.getClass.getName)

  AuthenticationService.createRole(new Role(1, "USER"))
  AuthenticationService.createRole(new Role(2, "REALTOR"))
  AuthenticationService.createRole(new Role(3, "ADMIN"))

  def UserAuthenticatedAction(f: (Request[AnyContent] => Result)) = Action { request =>
    val sessionString = request.headers.get("sessionString").getOrElse("")
    if(AuthenticationService.isSessionAuthenticated(sessionString)){
      f(request)
    } else {
      Unauthorized(s"SessionId is not Authenticated for this Endpoint")
    }
  }

  def AdminAuthenticatedAction(f: (Request[AnyContent] => Result)) = Action { request =>
    val sessionString = request.headers.get("sessionString").getOrElse("")
    if(AuthenticationService.isSessionAdmin(sessionString)){
      f(request)
    } else {
      Unauthorized(s"SessionId is not Authenticated for this Endpoint")
    }
  }

  def RealtorAuthenticatedAction(f: (Request[AnyContent] => Result)) = Action { request =>
    val sessionString: String = request.headers.get("sessionString").getOrElse("")
    if(AuthenticationService.isSessionReadWrite(sessionString)){
      f(request)
    } else {
      Unauthorized(s"SessionId is not Authenticated for this Endpoint")
    }
  }
}
