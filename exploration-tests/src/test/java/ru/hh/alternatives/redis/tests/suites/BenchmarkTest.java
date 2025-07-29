package ru.hh.alternatives.redis.tests.suites;

import com.redis.testcontainers.RedisContainer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.testcontainers.containers.GenericContainer;
import ru.hh.alternatives.redis.tests.benchmarks.Glide;
import ru.hh.alternatives.redis.tests.benchmarks.Jedis;
import ru.hh.alternatives.redis.tests.benchmarks.Lettuce;
import ru.hh.alternatives.redis.tests.benchmarks.Redisson;

public class BenchmarkTest {
  private static final GenericContainer<RedisContainer> redis = new GenericContainer<>("redis:8.0");
  private static final GenericContainer<RedisContainer> valkey = new GenericContainer<>("valkey/valkey:8.0");

  // See redis.conf and valkey.conf configuration documentation
  private static final String CONFIG_IN_DISK_LRU = "--maxmemory 256m --maxmemory-policy allkeys-lru --save 60 1000 --appendonly yes";
  private static final String CONFIG_IN_DISK_LFU = "--maxmemory 256m --maxmemory-policy allkeys-lfu --save 60 1000 --appendonly yes";
  private static final String CONFIG_IN_MEMORY_LRU = "--maxmemory 256m --maxmemory-policy allkeys-lru --save '' --appendonly no";
  private static final String CONFIG_IN_MEMORY_LFU = "--maxmemory 256m --maxmemory-policy allkeys-lfu --save '' --appendonly no";

  static {
    redis.setPortBindings(List.of("%d:%d/tcp".formatted(6379, 6379)));
    valkey.setPortBindings(List.of("%d:%d/tcp".formatted(6379, 6379)));
  }

  @Test
  public void redisInMemoryLRU() throws RunnerException {
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_MEMORY_LRU));

    redis.start();
    try {
      Options opt = new OptionsBuilder()
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(5)
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
    try {
      Options opt = new OptionsBuilder()
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(5)
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
    try {
      Options opt = new OptionsBuilder()
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(5)
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
    try {
      Options opt = new OptionsBuilder()
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(5)
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
    valkey.setCommand("redis-server %s".formatted(CONFIG_IN_MEMORY_LRU));

    valkey.start();
    try {
      Options opt = new OptionsBuilder()
          .include(Glide.class.getSimpleName())
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(5)
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
    try {
      Options opt = new OptionsBuilder()
          .include(Glide.class.getSimpleName())
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(5)
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
    try {
      Options opt = new OptionsBuilder()
          .include(Glide.class.getSimpleName())
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(5)
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
    try {
      Options opt = new OptionsBuilder()
          .include(Glide.class.getSimpleName())
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(5)
          .build();
      new Runner(opt).run();
    } finally {
      if (valkey.isRunning()) {
        valkey.stop();
      }
    }
  }
}
