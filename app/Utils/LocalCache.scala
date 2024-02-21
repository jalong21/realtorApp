package Utils

import net.sf.ehcache.{CacheManager, Element}

object LocalCache {

  private val cacheManager = CacheManager.getInstance()
  cacheManager.addCacheIfAbsent("LocalCache")
  private val cache = cacheManager.getCache("LocalCache")

  def getValue(key: Serializable) = cache.get(key)

  def setValue(key: Serializable, value: Serializable) =  cache.put(new Element(key, value))
}
