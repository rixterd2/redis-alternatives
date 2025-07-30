package ru.hh.alternatives.redis.explorationredisson.service;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class ExampleSerivce {

  private final RedissonClient client;

  public ExampleSerivce(String host, int port) {
    Config config = new Config();
    config.useSingleServer().setAddress("valkey://%s:%d".formatted(host, port));
    client = Redisson.create(config);
  }
}
