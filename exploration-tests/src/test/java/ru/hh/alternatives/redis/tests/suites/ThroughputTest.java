package ru.hh.alternatives.redis.tests.suites;

import com.redis.testcontainers.RedisContainer;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
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

  private static final long CACHE_SIZE_MB = 10240;
  // see redis.conf documentation
  private static final int IO_THREADS = Runtime.getRuntime().availableProcessors() - 1;

  // See redis.conf and valkey.conf configuration documentation
  // Enable disk but disable any writes, just to check difference (we are not able to run one behcmark for hour or go beyond 1 billion keys)
  private static final String CONFIG_IN_DISK_LRU = "--maxmemory %sm --io-threads %d --maxmemory-policy allkeys-lru --save 3600 1000000000 --appendonly yes".formatted(CACHE_SIZE_MB, IO_THREADS);
  private static final String CONFIG_IN_DISK_LFU = "--maxmemory %sm --io-threads %d --maxmemory-policy allkeys-lfu --save 3600 1000000000 --appendonly yes".formatted(CACHE_SIZE_MB, IO_THREADS);
  private static final String CONFIG_IN_MEMORY_LRU = "--maxmemory %sm --io-threads %d --maxmemory-policy allkeys-lru --save '' --appendonly no".formatted(CACHE_SIZE_MB, IO_THREADS);
  private static final String CONFIG_IN_MEMORY_LFU = "--maxmemory %sm --io-threads %d --maxmemory-policy allkeys-lfu --save '' --appendonly no".formatted(CACHE_SIZE_MB, IO_THREADS);

  static {
    redis.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
    valkey.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
  }

  @Test
  public void jfr() throws RunnerException {
    valkey.setCommand("valkey-server %s".formatted(CONFIG_IN_MEMORY_LRU));

    List<Class<?>> classes = List.of(
        GlideRead.class,
        GlideWrite.class,
        JedisRead.class,
        JedisWrite.class,
        LettuceRead.class,
        LettuceWrite.class,
        RedissonRead.class,
        RedissonWrite.class
    );

    valkey.start();
    try {
      for (Class<?> clazz : classes) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
          TearDown teardown = method.getAnnotation(TearDown.class);
          Setup setup = method.getAnnotation(Setup.class);
          if (teardown != null || setup != null) {
            continue;
          }
          String regexp = clazz.getSimpleName() + "." + method.getName();
          System.out.println(regexp);
          Options opt = new OptionsBuilder()
              .jvmArgs("-XX:StartFlightRecording=filename=%s.jfr,duration=60s".formatted(regexp))
              .include(regexp + "$")
              .warmupIterations(0)
              .measurementIterations(1)
              .measurementTime(TimeValue.seconds(60))
              .resultFormat(ResultFormatType.JSON)
              .forks(1)
              .build();
          new Runner(opt).run();
        }
      }
    } finally {
      if (valkey.isRunning()) {
        valkey.stop();
      }
    }
  }

  @Test
  public void redisInMemoryLRU() throws RunnerException {
    redis.setCommand("redis-server %s".formatted(CONFIG_IN_MEMORY_LRU));

    redis.start();
    try {
      Options opt = createBuilder()
          .result("redisInMemoryLRU-throughput.json")
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
      Options opt = createBuilder()
          .result("redisInMemoryLFU-throughput.json")
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
      Options opt = createBuilder()
          .result("redisInDiskLRU-throughput.json")
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
      Options opt = createBuilder()
          .result("redisInDiskLFU-throughput.json")
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
    try {
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInMemoryLRU-throughput.json")
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
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInMemoryLFU-throughput.json")
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
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInDiskLRU-throughput.json")
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
      Options opt = createBuilder()
          .include(GlideRead.class.getSimpleName())
          .include(GlideWrite.class.getSimpleName())
          .result("valkeyInDiskLFU-throughput.json")
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
        .threads(Runtime.getRuntime().availableProcessors())
        .resultFormat(ResultFormatType.JSON)
        .mode(Mode.Throughput)
        .forks(1);
  }
}
