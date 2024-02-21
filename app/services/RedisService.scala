package services

import play.api.libs.json.Json

import scala.util.{Failure, Success, Try, Using}
import redis.clients.jedis.{Jedis, JedisPool}

object RedisService {

  // setup for redis in docker is in the ReadMe
  val jedisPool = new JedisPool("redis-stack", 6379)

  def getValue(key: String)  = {

    // this seems weird, but Jedis isn't processing errors in a way that
    // scala is catching properly for some reason
    var resultString: Try[String] = Failure(new Throwable("Jedis Error"))
    try {
      resultString = Using(jedisPool.getResource) { jedis => jedis.get(key)} match {
        case Success(apartmentList) => Success(apartmentList)
        case Failure(_) => Failure(new Throwable("Jedis Error"))
      }
    } catch {
      case t: Throwable => Failure(new Throwable("Jedis Error"))
    }
    resultString

  }

  def setValue(key: String, value: String) =
    Using(jedisPool.getResource) { jedis => jedis.set(key, value)}

  def removeValue(key: String) =
    Using(jedisPool.getResource) { jedis => jedis.del(key)}
}
