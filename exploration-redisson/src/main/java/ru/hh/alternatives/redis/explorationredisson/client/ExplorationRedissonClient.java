package ru.hh.alternatives.redis.explorationredisson.client;

import org.redisson.config.Config;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;

public class RedissonClient implements KeyValueClient<String, String> {

  public RedissonClient(String host, int port) {
    Config config = new Config();
    config.useClusterServers()
        // use "redis://" for Redis connection
        // use "valkey://" for Valkey connection
        // use "valkeys://" for Valkey SSL connection
        // use "rediss://" for Redis SSL connection
        .addNodeAddress("redis://%s:%d".formatted(host, port));
  }

  @Override
  public String get(String key) {
    return "";
  }

  @Override
  public void set(String key, String value) {

  }

  @Override
  public void close() {

  }
}
