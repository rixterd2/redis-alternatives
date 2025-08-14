package ru.hh.alternatives.redis.tests.suites;

import com.redis.testcontainers.RedisContainer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
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
import ru.hh.alternatives.redis.tests.benchmarks.GlideRead;
import ru.hh.alternatives.redis.tests.benchmarks.GlideWrite;
import ru.hh.alternatives.redis.tests.benchmarks.JedisRead;
import ru.hh.alternatives.redis.tests.benchmarks.JedisWrite;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceRead;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceWrite;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonRead;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonWrite;

public class MemoryFreeTest extends AbstractBenchmark {
  private static final GenericContainer<RedisContainer> redis = new GenericContainer<>("redis:8.0");
  private static final GenericContainer<RedisContainer> valkey = new GenericContainer<>("valkey/valkey:8.0");
  private static final GenericContainer<RedisContainer> dragonflydb = new GenericContainer<>("docker.dragonflydb.io/dragonflydb/dragonfly:latest");

  private static final long CACHE_SIZE_GB = 2;

  // by default it uses all available cores ( 8 requires 2gb memory, 16 requires at least 4gb )
  private static final String DRAGONFLYDB_CONFIG = "--maxmemory %dg --proactor_threads %d --conn_io_threads %d".formatted(CACHE_SIZE_GB, 1, 1);

  // See redis.conf and valkey.conf configuration documentation
  private static final String CONFIG_IN_DISK_LRU = "--maxmemory %dg --maxmemory-policy allkeys-lru --save 3600 1000000000 --appendonly yes".formatted(
      CACHE_SIZE_GB);
  private static final String CONFIG_IN_DISK_LFU = "--maxmemory %dg --maxmemory-policy allkeys-lfu --save 3600 1000000000 --appendonly yes".formatted(
      CACHE_SIZE_GB);
  private static final String CONFIG_IN_MEMORY_LRU = "--maxmemory %dg --maxmemory-policy allkeys-lru --save '' --appendonly no".formatted(
      CACHE_SIZE_GB);
  private static final String CONFIG_IN_MEMORY_LFU = "--maxmemory %dg --maxmemory-policy allkeys-lfu --save '' --appendonly no".formatted(
      CACHE_SIZE_GB);

  @Test
  public void redisInMemoryLRU() {
    Options opt = createBuilder(REDIS_BENCHMARKS)
        .result("redisInMemoryLRU-memory-free.json")
        .build();

    this.withRedis(CONFIG_IN_MEMORY_LRU, () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @Test
  public void redisInMemoryLFU() {
    Options opt = createBuilder(REDIS_BENCHMARKS)
        .result("redisInMemoryLFU-memory-free.json")
        .build();

    this.withRedis(CONFIG_IN_MEMORY_LFU, () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @Test
  public void redisInDiskLRU() {
    Options opt = createBuilder(REDIS_BENCHMARKS)
        .result("redisInDiskLRU-memory-free.json")
        .build();

    this.withRedis(CONFIG_IN_DISK_LRU, () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @Test
  public void redisInDiskLFU() {
    Options opt = createBuilder(REDIS_BENCHMARKS)
        .result("redisInDiskLFU-memory-free.json")
        .build();

    this.withRedis(CONFIG_IN_DISK_LFU, () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @Test
  public void valkeyInMemoryLRU() {
    Options opt = createBuilder(VALKEY_BENCHMARKS)
        .result("valkeyInMemoryLRU-memory-free.json")
        .build();

    this.withValkey(CONFIG_IN_MEMORY_LRU, () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @Test
  public void valkeyInMemoryLFU() {
    Options opt = createBuilder(VALKEY_BENCHMARKS)
        .result("valkeyInMemoryLFU-memory-free.json")
        .build();

    this.withValkey(CONFIG_IN_MEMORY_LFU, () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @Test
  public void valkeyInDiskLRU() {
    Options opt = createBuilder(VALKEY_BENCHMARKS)
        .result("valkeyInDiskLRU-memory-free.json")
        .build();

    this.withValkey(CONFIG_IN_DISK_LRU, () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @Test
  public void valkeyInDiskLFU() {
    Options opt = createBuilder(VALKEY_BENCHMARKS)
        .result("valkeyInDiskLFU-memory-free.json")
        .build();

    this.withValkey(CONFIG_IN_DISK_LFU, () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @Test
  public void dragonflydb() {
    Options opt = createBuilder(DRAGONFLY_BENCHMARKS)
        .result("dragonflydb-memory-free.json")
        .build();

    this.withDragonfly(DRAGONFLYDB_CONFIG, () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  private static ChainedOptionsBuilder createBuilder(List<String> benchmarks) {
    ChainedOptionsBuilder builder = new OptionsBuilder()
        .warmupIterations(3)
        .warmupTime(TimeValue.seconds(5L))
        .measurementIterations(10)
        .measurementTime(TimeValue.seconds(5L))
        .resultFormat(ResultFormatType.JSON)
        .timeUnit(TimeUnit.NANOSECONDS)
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
