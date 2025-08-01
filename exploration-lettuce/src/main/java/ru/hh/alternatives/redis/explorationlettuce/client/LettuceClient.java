package ru.hh.alternatives.redis.explorationlettuce.client;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.Duration;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;

public class LettuceClient implements KeyValueClient<String, String> {
  private final RedisClient client;
  private final StatefulRedisConnection<String, String> connection;
  private final RedisCommands<String, String> commands;

  public LettuceClient(String host, int port) {
    client = RedisClient.create(new RedisURI(host, port, Duration.ofSeconds(1)));
    connection = client.connect();
    commands = connection.sync();
  }

  @Override
  public String get(String key) {
    return commands.get(key);
  }

  @Override
  public void set(String key, String value) {
    commands.set(key, value);
  }

  @Override
  public void setAndExpire(String key, String value, long seconds) {
    commands.setex(key, seconds, value);
  }

  @Override
  public void delete(String key) {
    commands.del(key);
  }

  @Override
  public void close() {
    // all connections will be closed automatically
    client.shutdown();
  }
}
