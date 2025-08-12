package ru.hh.alternatives.redis.tests.suites;

import com.redis.testcontainers.RedisContainer;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
  private static final GenericContainer<RedisContainer> dragonflydb = new GenericContainer<>("docker.dragonflydb.io/dragonflydb/dragonfly:latest");

  private static final long CACHE_SIZE_GB = 10;
  // see redis.conf documentation, main thread is not taken into account thus we have to reduce the value by 1
  private static final int IO_THREADS = Runtime.getRuntime().availableProcessors() - 1;

  private static final String DRAGONFLYDB_CONFIG = "--maxmemory %sg --proactor_threads %d --conn_io_threads %d".formatted(
      CACHE_SIZE_GB,
      IO_THREADS + 1,
      IO_THREADS + 1
  ); // unlike redis IO is total amount of all threads
  // See redis.conf and valkey.conf configuration documentation
  // Enable disk but disable any writes, just to check the difference (we are not able to run one behcmark for hour or go beyond 1 billion keys)
  private static final String CONFIG_IN_DISK_LRU = ("--maxmemory %sg --io-threads %d --maxmemory-policy allkeys-lru --save 3600 1000000000 " +
      "--appendonly yes").formatted(
      CACHE_SIZE_GB,
      IO_THREADS
  );
  private static final String CONFIG_IN_DISK_LFU = ("--maxmemory %sg --io-threads %d --maxmemory-policy allkeys-lfu --save 3600 1000000000 " +
      "--appendonly yes").formatted(
      CACHE_SIZE_GB,
      IO_THREADS
  );
  private static final String CONFIG_IN_MEMORY_LRU =
      "--maxmemory %sg --io-threads %d --maxmemory-policy allkeys-lru --save '' --appendonly no".formatted(
          CACHE_SIZE_GB,
          IO_THREADS
      );
  private static final String CONFIG_IN_MEMORY_LFU =
      "--maxmemory %sg --io-threads %d --maxmemory-policy allkeys-lfu --save '' --appendonly no".formatted(
          CACHE_SIZE_GB,
          IO_THREADS
      );

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
      Options opt = createBuilder(REDIS_BENCHMARKS)
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
      Options opt = createBuilder(REDIS_BENCHMARKS)
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
      Options opt = createBuilder(REDIS_BENCHMARKS)
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
      Options opt = createBuilder(REDIS_BENCHMARKS)
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
      Options opt = createBuilder(VALKEY_BENCHMARKS)
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
      Options opt = createBuilder(VALKEY_BENCHMARKS)
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
      Options opt = createBuilder(VALKEY_BENCHMARKS)
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
      Options opt = createBuilder(VALKEY_BENCHMARKS)
          .result("valkeyInDiskLFU-throughput.json")
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
          .result("dragonflydb-throughput.json")
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
        .warmupIterations(1)
        .warmupTime(TimeValue.seconds(10L))
        .measurementIterations(10)
        .measurementTime(TimeValue.seconds(20L))
        .resultFormat(ResultFormatType.JSON)
        .threads(Runtime.getRuntime().availableProcessors())
        .mode(Mode.Throughput)
        .timeUnit(TimeUnit.SECONDS)
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
}
