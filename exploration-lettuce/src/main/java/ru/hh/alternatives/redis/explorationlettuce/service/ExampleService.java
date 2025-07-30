package ru.hh.alternatives.redis.explorationlettuce.service;

import io.lettuce.core.KeyScanArgs;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.Duration;

public class ExampleService {

  private final RedisClient client;
  private final StatefulRedisConnection<String, String> connection;

  public ExampleService(String host, int port) {
    client = RedisClient.create(new RedisURI(host, port, Duration.ofSeconds(1)));
    connection = client.connect();
  }

  public boolean checkCachePopulation() {
    RedisCommands<String, String> commands = connection.sync();
    KeyScanCursor<String> scan = commands.scan(KeyScanArgs.Builder.matches("myCoolKeys*"));
    if (scan.getKeys().isEmpty()) {
      return false;
    }

    return true;
  }

  public void incrementCounter(String key) {
    RedisCommands<String, String> commands = connection.sync();
    commands.incr(key);
  }

  public void setValue(String key, String value) {
    RedisCommands<String, String> commands = connection.sync();
    commands.setex(key, Duration.ofSeconds(30).toSeconds(), value);
  }

  public String getValue(String key) {
    RedisCommands<String, String> commands = connection.sync();
    return commands.get(key);
  }

  public void deleteValue(String key) {
    RedisCommands<String, String> commands = connection.sync();
    commands.del(key);
  }
}
