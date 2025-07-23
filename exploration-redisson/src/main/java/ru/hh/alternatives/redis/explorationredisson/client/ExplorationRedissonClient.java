package ru.hh.alternatives.redis.explorationredisson.client;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;

public class ExplorationRedissonClient implements KeyValueClient<String, String> {
  private final RedissonClient client;
  private final RMap<String, String> holder;

  public ExplorationRedissonClient(String host, int port) {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://%s:%d".formatted(host, port));
    client = Redisson.create(config);
    holder = client.getMap("test");
  }

  @Override
  public String get(String key) {
    return holder.get(key);
  }

  @Override
  public void set(String key, String value) {
    holder.put(key, value);
  }

  @Override
  public void close() {
    client.shutdown();
  }
}
