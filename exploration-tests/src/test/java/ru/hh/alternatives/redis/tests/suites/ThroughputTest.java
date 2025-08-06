package ru.hh.alternatives.redis.tests.suites;

import com.redis.testcontainers.RedisContainer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.testcontainers.containers.GenericContainer;
import ru.hh.alternatives.redis.Constants;
import ru.hh.alternatives.redis.tests.benchmarks.GlideRead;
import ru.hh.alternatives.redis.tests.benchmarks.GlideWrite;
import ru.hh.alternatives.redis.tests.benchmarks.JedisRead;
import ru.hh.alternatives.redis.tests.benchmarks.JedisWrite;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceRead;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceWrite;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonRead;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonWrite;

public class ThroughputTest {
  private static final GenericContainer<RedisContainer> redis = new GenericContainer<>("redis:8.0");
  private static final GenericContainer<RedisContainer> valkey = new GenericContainer<>("valkey/valkey:8.0");

  // See redis.conf and valkey.conf configuration documentation
  // Enable disk but disable any writes, just to check difference (we are not able to run one behcmark for hour or go beyond 1 billion keys)
  private static final String CONFIG_IN_DISK_LRU = "--maxmemory 10240m --maxmemory-policy allkeys-lru --save 3600 1000000000 --appendonly yes";
  private static final String CONFIG_IN_DISK_LFU = "--maxmemory 10240m --maxmemory-policy allkeys-lfu --save 3600 1000000000 --appendonly yes";
  private static final String CONFIG_IN_MEMORY_LRU = "--maxmemory 10240m --maxmemory-policy allkeys-lru --save '' --appendonly no";
  private static final String CONFIG_IN_MEMORY_LFU = "--maxmemory 10240m --maxmemory-policy allkeys-lfu --save '' --appendonly no";

  static {
    redis.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
    valkey.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
  }

  @Test
  public void redisInMemoryLRU() throws RunnerException {
    System.out.println("Starting redisInMemoryLRU");
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_MEMORY_LRU));

    redis.start();
    try {
      Options opt = createBuilder()
          .result("redisInMemoryLRU.json")
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
    System.out.println("Starting redisInMemoryLFU");
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_MEMORY_LFU));

    redis.start();
    try {
      Options opt = createBuilder()
          .result("redisInMemoryLFU.json")
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
    System.out.println("Starting redisInDiskLRU");
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_DISK_LRU));

    redis.start();
    try {
      Options opt = createBuilder()
          .result("redisInDiskLRU.json")
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
    System.out.println("Starting redisInDiskLFU");
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_DISK_LFU));

    redis.start();
    try {
      Options opt = createBuilder()
          .result("redisInDiskLFU.json")
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
    System.out.println("Starting valkeyInMemoryLRU");
    valkey.setCommand("valkey-server %s".formatted(CONFIG_IN_MEMORY_LRU));

    valkey.start();
    try {
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInMemoryLRU.json")
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
    System.out.println("Starting valkeyInMemoryLFU");
    valkey.setCommand("valkey-server %s".formatted(CONFIG_IN_MEMORY_LFU));

    valkey.start();
    try {
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInMemoryLFU.json")
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
    System.out.println("Starting valkeyInDiskLRU");
    valkey.setCommand("valkey-server %s".formatted(CONFIG_IN_DISK_LRU));

    valkey.start();
    try {
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInDiskLRU.json")
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
    System.out.println("Starting valkeyInDiskLFU");
    valkey.setCommand("valkey-server %s".formatted(CONFIG_IN_DISK_LFU));

    valkey.start();
    try {
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInDiskLFU.json")
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
        .warmupIterations(0)
        .measurementIterations(1)
        .measurementTime(TimeValue.seconds(60L))
        .resultFormat(ResultFormatType.JSON)
        .mode(Mode.Throughput)
        .forks(1);
  }
}
