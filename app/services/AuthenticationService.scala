package services

import models.{Role, SessionToken, User}
import play.api.Logger
import play.api.libs.json.Json

import java.util.UUID
import scala.util.{Failure, Success}

object AuthenticationService {

  val log = Logger(this.getClass.getName)

  def getSessionTokenForUserPass(username: String, password: String) = {
    getUser(User.getUserId(username, password))
      .map(user => {
        val session = new SessionToken(UUID.randomUUID().toString, User.getUserId(user), user.roleId, System.currentTimeMillis())
        log.warn(s"creating session token: ${session.token}")
        createSession(session)
        session.token
    })
  }

  def createUser(user: User) = RedisService
    .setValue(User.getUserId(user.username, user.password), Json.toJson(user).toString())

  def getUser(userId: String): Option[User] = {
    log.warn(s"getting user: $userId")
    RedisService.getValue(userId) match {
      case Success(userString) => Json.fromJson[User](Json.parse(userString)).asOpt
      case Failure(throwable) => None
    }
  }

  def createRole(role: Role) = RedisService
    .setValue(Role.getStorageId(role.roleId), Json.toJson(role).toString())

  def getRole(roleId: Int): Option[Role] = RedisService.getValue(Role.getStorageId(roleId)) match {
    case Success(roleString) => Json.fromJson[Role](Json.parse(roleString)).asOpt
    case Failure(throwable) => None
  }

  def createSession(session: SessionToken) = RedisService
    .setValue(SessionToken.getStorageId(session.token), Json.toJson(session).toString())

  def getSession(sessionId: String): Option[SessionToken] = RedisService.getValue(SessionToken.getStorageId(sessionId)) match {
    case Success(sessionString) => Json.fromJson[SessionToken](Json.parse(sessionString)).asOpt
    case Failure(throwable) => None
  }

  def getUserForSession(sessionId: String): Option[User] = RedisService.getValue(SessionToken.getStorageId(sessionId)) match {
    case Success(sessionString) => Json.fromJson[SessionToken](Json.parse(sessionString)).asOpt
      .flatMap(sessionToken => getUser(sessionToken.userId))
    case Failure(throwable) => None
  }

  def isUserAdmin(user: User): Boolean = getRole(user.roleId)
    .filter(role => Role.ADMIN.equals(role.roleId))
    .isDefined

  def isUserRealtor(user: User): Boolean = getRole(user.roleId)
    .filter(role => Role.REALTOR.equals(role.roleId))
    .isDefined

  def isUserBasic(user: User): Boolean = getRole(user.roleId)
    .filter(role => Role.USER.equals(role.roleId))
    .isDefined

  def isSessionAdmin(sessionId: String) = getSession(sessionId)
    .flatMap(session => getRole(session.roleId)
      .filter(role => Role.ADMIN.equals(role.roleId)))
    .isDefined

  def isSessionRealtor(sessionId: String) = getSession(sessionId)
    .flatMap(session => getRole(session.roleId)
      .filter(role => Role.REALTOR.equals(role.roleId)))
    .isDefined

  def isSessionBasic(sessionId: String) = getSession(sessionId)
    .flatMap(session => getRole(session.roleId)
      .filter(role => Role.USER.equals(role.roleId)))
    .isDefined

  def isSessionAuthenticated(sessionId: String) = isSessionBasic(sessionId) || isSessionRealtor(sessionId) || isSessionAdmin(sessionId)

  def isSessionReadWrite(sessionId: String) = isSessionRealtor(sessionId) || isSessionAdmin(sessionId)
}
