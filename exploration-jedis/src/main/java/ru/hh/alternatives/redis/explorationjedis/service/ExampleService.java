package ru.hh.alternatives.redis.explorationjedis.service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ExampleService {

  private final JedisPool jedisPool;
  private final Jedis resource;

  public ExampleService(String host, int port) {
    jedisPool = new JedisPool(host, port);
    resource = jedisPool.getResource();
  }

}
