package ru.hh.alternatives.redis.explorationjedis.client;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;

public class JedisClient implements KeyValueClient<String, String> {
  private final JedisPool jedisPool;

  public JedisClient(String host, int port) {
    GenericObjectPoolConfig<Jedis> config = new GenericObjectPoolConfig<>();
    config.setMaxTotal(Runtime.getRuntime().availableProcessors() * 4);
    config.setTestOnBorrow(true);
    config.setTestOnReturn(true);
    jedisPool = new JedisPool(config, host, port);
  }

  @Override
  public String get(String key) {
    try (var resource = jedisPool.getResource()) {
      return resource.get(key);
    }
  }

  @Override
  public void set(String key, String value) {
    try (var resource = jedisPool.getResource()) {
      resource.set(key, value);
    }
  }

  @Override
  public void setAndExpire(String key, String value, long seconds) {
    try (var resource = jedisPool.getResource()) {
      resource.setex(key, seconds, value);
    }
  }

  @Override
  public void delete(String key) {
    try (var resource = jedisPool.getResource()) {
      resource.del(key);
    }
  }

  @Override
  public void close() {
    this.jedisPool.close();
  }
}
