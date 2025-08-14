package ru.hh.alternatives.redis.tests.suites;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
  private static final String CACHE_SIZE = "10g";
  // see redis.conf documentation, main thread is not taken into account thus we have to reduce the value by 1
  private static final int IO_THREADS = Runtime.getRuntime().availableProcessors();

  private static Stream<Arguments> redisConfigs() {
    String container = "redis";
    RedisConfig diskRedisConfig = new RedisConfig().onDisk().withThreads(IO_THREADS);
    RedisConfig memoryRedisConfig = new RedisConfig().inMemory().withThreads(IO_THREADS);
    return Stream.of(
        Arguments.of(
            "%s-%s-disk-lru".formatted(ThroughputTest.class.getSimpleName(), container),
            diskRedisConfig.withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lru-1core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().onDisk().withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lru-4core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().onDisk().withThreads(4).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lru-8core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().onDisk().withThreads(8).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lru-12core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().onDisk().withThreads(12).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lfu".formatted(ThroughputTest.class.getSimpleName(), container),
            diskRedisConfig.withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lfu")
        ),
        Arguments.of(
            "%s-%s-inmemory-lru".formatted(ThroughputTest.class.getSimpleName(), container),
            memoryRedisConfig.withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-inmemory-lru-1core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().inMemory().withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-inmemory-lfu".formatted(ThroughputTest.class.getSimpleName(), container),
            memoryRedisConfig.withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lfu")
        )
    );
  }

  private static Stream<Arguments> valkeyConfigs() {
    String container = "valkey";
    RedisConfig diskRedisConfig = new RedisConfig().onDisk().withThreads(IO_THREADS);
    RedisConfig memoryRedisConfig = new RedisConfig().inMemory().withThreads(IO_THREADS);
    return Stream.of(
        Arguments.of(
            "%s-%s-disk-lru".formatted(ThroughputTest.class.getSimpleName(), container),
            diskRedisConfig.withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lru-1core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().onDisk().withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lru-4core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().onDisk().withThreads(4).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lru-8core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().onDisk().withThreads(8).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lru-12core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().onDisk().withThreads(12).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lfu".formatted(ThroughputTest.class.getSimpleName(), container),
            diskRedisConfig.withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lfu")
        ),
        Arguments.of(
            "%s-%s-inmemory-lru".formatted(ThroughputTest.class.getSimpleName(), container),
            memoryRedisConfig.withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-inmemory-lru-1core".formatted(ThroughputTest.class.getSimpleName(), container),
            new RedisConfig().inMemory().withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-inmemory-lfu".formatted(ThroughputTest.class.getSimpleName(), container),
            memoryRedisConfig.withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lfu")
        )
    );
  }

  private static Stream<Arguments> dragonflyConfigs() {
    DragonflyConfig diskRedisConfig = new DragonflyConfig().withThreads(IO_THREADS);
    return Stream.of(
        Arguments.of(
            "%s-dragonfly-disk-eviction".formatted(ThroughputTest.class.getSimpleName()),
            diskRedisConfig.withMemory(CACHE_SIZE).withEviction()
        ),
        Arguments.of(
            "%s-dragonfly-disk-eviction-1core".formatted(ThroughputTest.class.getSimpleName()),
            new DragonflyConfig().withThreads(1).withMemory(CACHE_SIZE).withEviction()
        ),
        Arguments.of(
            "%s-dragonfly-disk-eviction-4core".formatted(ThroughputTest.class.getSimpleName()),
            new DragonflyConfig().withThreads(4).withMemory(CACHE_SIZE).withEviction()
        ),
        Arguments.of(
            "%s-dragonfly-disk-eviction-8core".formatted(ThroughputTest.class.getSimpleName()),
            new DragonflyConfig().withThreads(8).withMemory(CACHE_SIZE).withEviction()
        ),
        Arguments.of(
            "%s-dragonfly-disk-eviction-12core".formatted(ThroughputTest.class.getSimpleName()),
            new DragonflyConfig().withThreads(12).withMemory(CACHE_SIZE).withEviction()
        ),
        Arguments.of(
            "%s-dragonfly-disk-noeviction".formatted(ThroughputTest.class.getSimpleName()),
            diskRedisConfig.withMemory(CACHE_SIZE)
        )
    );
  }

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

    RedisConfig config = new RedisConfig()
        .inMemory()
        .withThreads(IO_THREADS)
        .withMemory(CACHE_SIZE)
        .withEvictionPolicy("allkeys-lru");

    this.withValkey(config.toString(), () -> {
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

  @ParameterizedTest
  @MethodSource("redisConfigs")
  public void redis(String name, RedisConfig redisConfig) {
    Options opt = createBuilder(REDIS_BENCHMARKS).result(name).build();

    this.withRedis(redisConfig.toString(), () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @ParameterizedTest
  @MethodSource("valkeyConfigs")
  public void valkey(String name, RedisConfig redisConfig) {
    Options opt = createBuilder(VALKEY_BENCHMARKS).result(name).build();

    this.withValkey(redisConfig.toString(), () -> {
      try {
        new Runner(opt).run();
      } catch (RunnerException e) {
        Assertions.fail(e.getMessage());
      }
    });
  }

  @ParameterizedTest
  @MethodSource("dragonflyConfigs")
  public void dragonfly(String name, DragonflyConfig dragonflyConfig) {
    Options opt = createBuilder(DRAGONFLY_BENCHMARKS).result(name).build();

    this.withDragonfly(dragonflyConfig.toString(), () -> {
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
