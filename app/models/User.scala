package models

import play.api.libs.json.{Json, Reads, Writes}

case class NewUser(username: String, email: String, password: String)
object NewUser {
  implicit val jsonReads: Reads[NewUser] = Json.reads[NewUser]
  implicit val jsonWrites: Writes[NewUser] = Json.writes[NewUser]
}

case class User(roleId: Int, username: String, email: String, password: String)
object User {
  implicit val jsonReads: Reads[User] = Json.reads[User]
  implicit val jsonWrites: Writes[User] = Json.writes[User]
  def getUserId(user: User): String = getUserId(user.username, user.password)
  def getUserId(username: String, password: String):String = s"User-$username,$password"
}

case class Role(roleId: Int, roleDescription: String)
object Role {
  implicit val jsonReads: Reads[Role] = Json.reads[Role]
  implicit val jsonWrites: Writes[Role] = Json.writes[Role]

  def getStorageId(id: Int) = s"Role-$id"

  val USER = 1
  val REALTOR = 2
  val ADMIN = 3
}

case class SessionToken(token: String, userId: String, roleId: Int, timestamp: Long)
object SessionToken {
  implicit val jsonReads: Reads[SessionToken] = Json.reads[SessionToken]
  implicit val jsonWrites: Writes[SessionToken] = Json.writes[SessionToken]

  def getStorageId(token: String) = s"SessionToken-$token"
}
