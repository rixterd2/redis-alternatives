package ru.hh.alternatives.redis.tests.suites;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
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
import ru.hh.alternatives.redis.tests.benchmarks.GlideRead;
import ru.hh.alternatives.redis.tests.benchmarks.GlideWrite;
import ru.hh.alternatives.redis.tests.benchmarks.JedisRead;
import ru.hh.alternatives.redis.tests.benchmarks.JedisWrite;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceRead;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceWrite;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonRead;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonWrite;

public class ThroughputTest extends AbstractBenchmark {
  private static final long CACHE_SIZE_GB = 10;
  // see redis.conf documentation, main thread is not taken into account thus we have to reduce the value by 1
  private static final int IO_THREADS = Runtime.getRuntime().availableProcessors() - 1;

  // by default it uses all available cores ( 8 requires 2gb memory, 16 requires at least 4gb )
  private static final String DRAGONFLYDB_CONFIG = "--maxmemory %dg --proactor_threads %d --conn_io_threads %d".formatted(
      CACHE_SIZE_GB,
      IO_THREADS + 1,
      IO_THREADS + 1
  ); // unlike redis IO is total amount of all threads

  // See redis.conf and valkey.conf configuration documentation
  // Enable disk but disable any writes, just to check the difference (we are not able to run one behcmark for hour or go beyond 1 billion keys)
  private static final String CONFIG_IN_DISK_LRU = ("--maxmemory %dg --io-threads %d --maxmemory-policy allkeys-lru --save 3600 1000000000 " +
      "--appendonly yes").formatted(
      CACHE_SIZE_GB,
      IO_THREADS
  );
  private static final String CONFIG_IN_DISK_LFU = ("--maxmemory %dg --io-threads %d --maxmemory-policy allkeys-lfu --save 3600 1000000000 " +
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

  @Test
  public void jfr() {
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

    this.withValkey(CONFIG_IN_MEMORY_LRU, () -> {
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
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @Test
  public void redisInMemoryLRU() {
    Options opt = createBuilder(REDIS_BENCHMARKS)
        .result("redisInMemoryLRU-throughput.json")
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
        .result("redisInMemoryLFU-throughput.json")
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
        .result("redisInDiskLRU-throughput.json")
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
        .result("redisInDiskLFU-throughput.json")
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
        .result("valkeyInMemoryLRU-throughput.json")
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
        .result("valkeyInMemoryLFU-throughput.json")
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
        .result("valkeyInDiskLRU-throughput.json")
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
        .result("valkeyInDiskLFU-throughput.json")
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
        .result("dragonflydb-throughput.json")
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
