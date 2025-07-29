package ru.hh.alternatives.redis.explorationredisson.client;

import java.util.Map;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;

public class ExplorationRedissonClient implements KeyValueClient<String, String> {
  private final RedissonClient client;

  public ExplorationRedissonClient(String host, int port) {
    Config config = new Config();
    config.useSingleServer().setAddress("valkey://%s:%d".formatted(host, port));
    client = Redisson.create(config);
  }

  @Override
  public String get(String key) {
    return client.<String>getBucket(key).get();
  }

  @Override
  public void set(String key, String value) {
    client.<String>getBucket(key).set(value);
  }

  @Override
  public void delete(String key) {
    client.getBucket(key).delete();
  }

  @Override
  public void close() {
    client.shutdown();
  }
}
