package controllers

import models.{NewUser, Role, User}
import play.api.libs.json.Json
import play.api.mvc._
import services.AuthenticationService

import java.util.{Base64, UUID}
import javax.inject.Inject


class BasicAuthController @Inject()(cc: ControllerComponents) extends AuthenticatiedController(cc) {

  AuthenticationService.createUser(new User(Role.ADMIN, "Austin", "email", "admin" ))

  def getUser = AdminAuthenticatedAction {
    request => {
      val submittedCredentials: Option[List[String]] = for {
        authHeader <- request.headers.get("Authorization")
        parts <- authHeader.split(' ').drop(1).headOption
      } yield new String(Base64.getDecoder.decode(parts.getBytes)).split(':').toList

      submittedCredentials.flatMap(creds => {
        AuthenticationService.getUser(User.getUserId(creds.head, creds.last))
      }).map(user => Ok(Json.toJson[User](user)))
        .getOrElse(BadRequest("Something went wrong"))
    }
  }
  // anyone can hit the login endopint, and get a session token
  def login = Action {
    request => {
      log.warn("logging in")
      val submittedCredentials: Option[List[String]] = for {
        authHeader <- request.headers.get("Authorization")
        _ = log.warn(s"authHeader: $authHeader")
        parts <- authHeader.split(' ').drop(1).headOption
      } yield new String(Base64.getDecoder.decode(parts.getBytes)).split(':').toList


      submittedCredentials.flatMap(creds => {
        log.warn(s"split header: ${creds.head}, ${creds.last}")
        AuthenticationService.getSessionTokenForUserPass(creds.head, creds.last)
      }).map(sessionToken => Ok(sessionToken))
        .getOrElse(BadRequest("Something went wrong"))
    }
  }

  // anyone can register a new user
  def registerUser = Action {
    request => request.body.asJson
      .map( requestBody => {
        log.warn(s"requestBody: ${requestBody.toString()}")
        Json.fromJson[NewUser](requestBody)
          .map(user => new User(Role.USER, user.username, user.email, user.password))
          .map(user => {
            AuthenticationService.createUser(user)
            Ok(Json.toJson[User](user))
          })
          .getOrElse(BadRequest("Not able to generate new user"))
      }).getOrElse(BadRequest("Payload Missing From Request"))
  }

  // only an admin can register a new realtor
  def registerRealtor() = AdminAuthenticatedAction {
    request => request.body.asJson
      .map( requestBody => {
        log.warn(s"requestBody: ${requestBody.toString()}")
        val newUser = Json.fromJson[NewUser](requestBody).get
        val createdUser = new User(Role.REALTOR, newUser.username, newUser.email, newUser.password)
        AuthenticationService.createUser(createdUser)
        Ok(Json.toJson[User](createdUser))
      }).getOrElse(BadRequest("Payload Missing From Request"))
  }
}