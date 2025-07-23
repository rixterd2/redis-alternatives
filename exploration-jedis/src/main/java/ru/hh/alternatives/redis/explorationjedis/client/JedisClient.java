package ru.hh.alternatives.redis.explorationjedis.client;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;

public class JedisClient implements KeyValueClient<String, String> {
  private final JedisPool jedisPool;
  private final Jedis resource;

  public JedisClient(String host, int port) {
    jedisPool = new JedisPool(host, port);
    resource = jedisPool.getResource();
  }

  @Override
  public String get(String key) {
    return resource.get(key);
  }

  @Override
  public void set(String key, String value) {
    resource.set(key, value);
  }

  @Override
  public void close() {
    this.resource.close();
    this.jedisPool.close();
  }
}
