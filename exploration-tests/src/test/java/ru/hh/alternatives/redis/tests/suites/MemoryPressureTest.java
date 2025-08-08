package ru.hh.alternatives.redis.tests.suites;

import com.redis.testcontainers.RedisContainer;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.testcontainers.containers.GenericContainer;
import ru.hh.alternatives.redis.Constants;
import ru.hh.alternatives.redis.Utils;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;
import ru.hh.alternatives.redis.explorationjedis.client.JedisClient;
import ru.hh.alternatives.redis.tests.benchmarks.GlideRead;
import ru.hh.alternatives.redis.tests.benchmarks.GlideWrite;
import ru.hh.alternatives.redis.tests.benchmarks.JedisRead;
import ru.hh.alternatives.redis.tests.benchmarks.JedisWrite;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceRead;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceWrite;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonRead;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonWrite;

public class MemoryPressureTest {
  private static final GenericContainer<RedisContainer> redis = new GenericContainer<>("redis:8.0");
  private static final GenericContainer<RedisContainer> valkey = new GenericContainer<>("valkey/valkey:8.0");

  private static final long CACHE_SIZE_MB = 256;

  // See redis.conf and valkey.conf configuration documentation
  private static final String CONFIG_IN_DISK_LRU = "--maxmemory %sm --maxmemory-policy allkeys-lru --save 60 1000 --appendonly yes".formatted(CACHE_SIZE_MB);
  private static final String CONFIG_IN_DISK_LFU = "--maxmemory %sm --maxmemory-policy allkeys-lfu --save 60 1000 --appendonly yes".formatted(CACHE_SIZE_MB);
  private static final String CONFIG_IN_MEMORY_LRU = "--maxmemory %sm --maxmemory-policy allkeys-lru --save '' --appendonly no".formatted(CACHE_SIZE_MB);
  private static final String CONFIG_IN_MEMORY_LFU = "--maxmemory %sm --maxmemory-policy allkeys-lfu --save '' --appendonly no".formatted(CACHE_SIZE_MB);

  static {
    redis.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
    valkey.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
  }

  @Test
  public void redisInMemoryLRU() throws RunnerException {
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_MEMORY_LRU));

    redis.start();
    setupMemoryPressure();
    try {
      Options opt = createBuilder()
          .result("redisInMemoryLRU-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (redis.isRunning()) {
        redis.stop();
      }
    }
  }

  @Test
  public void redisInMemoryLFU() throws RunnerException {
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_MEMORY_LFU));

    redis.start();
    setupMemoryPressure();
    try {
      Options opt = createBuilder()
          .result("redisInMemoryLFU-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (redis.isRunning()) {
        redis.stop();
      }
    }
  }

  @Test
  public void redisInDiskLRU() throws RunnerException {
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_DISK_LRU));

    redis.start();
    setupMemoryPressure();
    try {
      Options opt = createBuilder()
          .result("redisInDiskLRU-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (redis.isRunning()) {
        redis.stop();
      }
    }
  }

  @Test
  public void redisInDiskLFU() throws RunnerException {
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_DISK_LFU));

    redis.start();
    setupMemoryPressure();
    try {
      Options opt = createBuilder()
          .result("redisInDiskLFU-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (redis.isRunning()) {
        redis.stop();
      }
    }
  }

  @Test
  public void valkeyInMemoryLRU() throws RunnerException {
    valkey.setCommand("valkey-server %s".formatted(CONFIG_IN_MEMORY_LRU));

    valkey.start();
    setupMemoryPressure();
    try {
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInMemoryLRU-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (valkey.isRunning()) {
        valkey.stop();
      }
    }
  }

  @Test
  public void valkeyInMemoryLFU() throws RunnerException {
    valkey.setCommand("valkey-server %s".formatted(CONFIG_IN_MEMORY_LFU));

    valkey.start();
    setupMemoryPressure();
    try {
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInMemoryLFU-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (valkey.isRunning()) {
        valkey.stop();
      }
    }
  }

  @Test
  public void valkeyInDiskLRU() throws RunnerException {
    valkey.setCommand("valkey-server %s".formatted(CONFIG_IN_DISK_LRU));

    valkey.start();
    setupMemoryPressure();
    try {
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInDiskLRU-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (valkey.isRunning()) {
        valkey.stop();
      }
    }
  }

  @Test
  public void valkeyInDiskLFU() throws RunnerException {
    valkey.setCommand("valkey-server %s".formatted(CONFIG_IN_DISK_LFU));

    valkey.start();
    setupMemoryPressure();
    try {
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInDiskLFU-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (valkey.isRunning()) {
        valkey.stop();
      }
    }
  }

  private static ChainedOptionsBuilder createBuilder() {
    return new OptionsBuilder()
        .include(JedisRead.class.getSimpleName())
        .include(LettuceRead.class.getSimpleName())
        .include(RedissonRead.class.getSimpleName())
        .include(JedisWrite.class.getSimpleName())
        .include(LettuceWrite.class.getSimpleName())
        .include(RedissonWrite.class.getSimpleName())
        .warmupIterations(5)
        .measurementIterations(10)
        .resultFormat(ResultFormatType.JSON)
        .forks(1);
  }

  public void setupMemoryPressure() {
    KeyValueClient<String, String> jedis = new JedisClient(Constants.HOST, Constants.PORT);
    for (int i = 0; i < CACHE_SIZE_MB; i++) {
      jedis.set(UUID.randomUUID().toString(), Utils.generateString(Constants.MB_1));
    }
    jedis.close();
  }
}
