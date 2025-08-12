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
import org.openjdk.jmh.runner.options.TimeValue;
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
  private static final GenericContainer<RedisContainer> dragonflydb = new GenericContainer<>("docker.dragonflydb.io/dragonflydb/dragonfly:latest");

  private static final long CACHE_SIZE_GB = 2;

  private static final String DRAGONFLYDB_CONFIG = "--maxmemory %sg".formatted(CACHE_SIZE_GB);

  // See redis.conf and valkey.conf configuration documentation
  private static final String CONFIG_IN_DISK_LRU = "--maxmemory %sg --maxmemory-policy allkeys-lru --save 60 1000 --appendonly yes".formatted(
      CACHE_SIZE_GB);
  private static final String CONFIG_IN_DISK_LFU = "--maxmemory %sg --maxmemory-policy allkeys-lfu --save 60 1000 --appendonly yes".formatted(
      CACHE_SIZE_GB);
  private static final String CONFIG_IN_MEMORY_LRU = "--maxmemory %sg --maxmemory-policy allkeys-lru --save '' --appendonly no".formatted(
      CACHE_SIZE_GB);
  private static final String CONFIG_IN_MEMORY_LFU = "--maxmemory %sg --maxmemory-policy allkeys-lfu --save '' --appendonly no".formatted(
      CACHE_SIZE_GB);

  private static final List<String> REDIS_BENCHMARKS = List.of(
      JedisRead.class.getSimpleName(),
      LettuceRead.class.getSimpleName(),
      RedissonRead.class.getSimpleName(),
      JedisWrite.class.getSimpleName(),
      LettuceWrite.class.getSimpleName(),
      RedissonWrite.class.getSimpleName()
  );

  private static final List<String> VALKEY_BENCHMARKS = List.of(
      GlideRead.class.getSimpleName(),
      JedisRead.class.getSimpleName(),
      LettuceRead.class.getSimpleName(),
      RedissonRead.class.getSimpleName(),
      GlideWrite.class.getSimpleName(),
      JedisWrite.class.getSimpleName(),
      LettuceWrite.class.getSimpleName(),
      RedissonWrite.class.getSimpleName()
  );

  static {
    redis.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
    valkey.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
    dragonflydb.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
  }

  @Test
  public void redisInMemoryLRU() throws RunnerException {
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_MEMORY_LRU));

    redis.start();
    setupMemoryPressure();
    try {
      Options opt = createBuilder(REDIS_BENCHMARKS)
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
      Options opt = createBuilder(REDIS_BENCHMARKS)
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
      Options opt = createBuilder(REDIS_BENCHMARKS)
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
      Options opt = createBuilder(REDIS_BENCHMARKS)
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
      Options opt = createBuilder(VALKEY_BENCHMARKS)
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
      Options opt = createBuilder(VALKEY_BENCHMARKS)
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
      Options opt = createBuilder(VALKEY_BENCHMARKS)
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
      Options opt = createBuilder(VALKEY_BENCHMARKS)
          .result("valkeyInDiskLFU-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (valkey.isRunning()) {
        valkey.stop();
      }
    }
  }

  @Test
  public void dragonflydb() throws RunnerException {
    dragonflydb.setCommand("dragonfly %s".formatted(DRAGONFLYDB_CONFIG));

    dragonflydb.start();
    try {
      Options opt = createBuilder(REDIS_BENCHMARKS)
          .result("dragonflydb-memory-pressure.json")
          .build();
      new Runner(opt).run();
    } finally {
      if (dragonflydb.isRunning()) {
        dragonflydb.stop();
      }
    }
  }

  private static ChainedOptionsBuilder createBuilder(List<String> benchmarks) {
    ChainedOptionsBuilder builder = new OptionsBuilder()
        .warmupIterations(10)
        .warmupTime(TimeValue.seconds(1))
        .measurementIterations(10)
        .measurementTime(TimeValue.seconds(1))
        .resultFormat(ResultFormatType.JSON)
        .jvmArgsAppend("-XX:+UnlockExperimentalVMOptions")
        .jvmArgsAppend("-XX:+UseEpsilonGC")
        .jvmArgsAppend("-Xmx10g")
        .jvmArgsAppend("-Xms10g")
        .jvmArgsAppend("-XX:+AlwaysPreTouch")
        .forks(1);

    for (String benchmark : benchmarks) {
      builder.include(benchmark);
    }

    return builder;
  }

  public void setupMemoryPressure() {
    KeyValueClient<String, String> jedis = new JedisClient(Constants.HOST, Constants.PORT);
    // 2 just in case to fill up the memory
    long keyCount = CACHE_SIZE_GB * 1024 * 2;
    for (int i = 0; i < keyCount; i++) {
      jedis.set(UUID.randomUUID().toString(), Constants.VALUE_1MB);
    }
    jedis.close();
  }
}
