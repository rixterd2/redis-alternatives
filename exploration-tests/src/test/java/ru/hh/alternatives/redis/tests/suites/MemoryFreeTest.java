package ru.hh.alternatives.redis.tests.suites;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class MemoryFreeTest extends AbstractBenchmark {
  private static final String CACHE_SIZE = "2g";

  // by default it uses all available cores ( 8 requires 2gb memory, 16 requires at least 4gb )
  private static final String DRAGONFLYDB_CONFIG = "--maxmemory %dg --proactor_threads %d --conn_io_threads %d".formatted(CACHE_SIZE, 1, 1);

  private static Stream<Arguments> redisConfigs() {
    String container = "redis";
    RedisConfig diskRedisConfig = new RedisConfig().onDisk();
    RedisConfig memoryRedisConfig = new RedisConfig().inMemory();
    return Stream.of(
        Arguments.of(
            "%s-%s-disk-lru".formatted(MemoryFreeTest.class.getSimpleName(), container),
            diskRedisConfig.withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lfu".formatted(MemoryFreeTest.class.getSimpleName(), container),
            diskRedisConfig.withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lfu")
        ),
        Arguments.of(
            "%s-%s-inmemory-lru".formatted(MemoryFreeTest.class.getSimpleName(), container),
            memoryRedisConfig.withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-inmemory-lfu".formatted(MemoryFreeTest.class.getSimpleName(), container),
            memoryRedisConfig.withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lfu")
        )
    );
  }

  private static Stream<Arguments> valkeyConfigs() {
    String container = "valkey";
    RedisConfig diskRedisConfig = new RedisConfig().onDisk();
    RedisConfig memoryRedisConfig = new RedisConfig().inMemory();
    return Stream.of(
        Arguments.of(
            "%s-%s-disk-lru".formatted(MemoryFreeTest.class.getSimpleName(), container),
            diskRedisConfig.withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lfu".formatted(MemoryFreeTest.class.getSimpleName(), container),
            diskRedisConfig.withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lfu")
        ),
        Arguments.of(
            "%s-%s-inmemory-lru".formatted(MemoryFreeTest.class.getSimpleName(), container),
            memoryRedisConfig.withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-inmemory-lfu".formatted(MemoryFreeTest.class.getSimpleName(), container),
            memoryRedisConfig.withThreads(1).withMemory(CACHE_SIZE).withEvictionPolicy("allkeys-lfu")
        )
    );
  }

  private static Stream<Arguments> dragonflyConfigs() {
    DragonflyConfig diskRedisConfig = new DragonflyConfig().onDisk();
    return Stream.of(
        Arguments.of(
            "%s-dragonfly-disk-eviction".formatted(MemoryFreeTest.class.getSimpleName()),
            diskRedisConfig.withThreads(1).withMemory(CACHE_SIZE).withEviction()
        ),
        Arguments.of(
            "%s-dragonfly-disk-noeviction".formatted(MemoryFreeTest.class.getSimpleName()),
            diskRedisConfig.withThreads(1).withMemory(CACHE_SIZE)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("redisConfigs")
  public void redis(String name, RedisConfig redisConfig) {
    Options opt = createBuilder(REDIS_BENCHMARKS).result(name).build();

    this.withRedis(
        redisConfig.toString(), () -> {
          try {
            new Runner(opt).run();
          } catch (RunnerException e) {
            Assertions.fail(e.getMessage());
          }
        }
    );
  }

  @ParameterizedTest
  @MethodSource("valkeyConfigs")
  public void valkey(String name, RedisConfig redisConfig) {
    Options opt = createBuilder(VALKEY_BENCHMARKS).result(name).build();

    this.withValkey(
        redisConfig.toString(), () -> {
          try {
            new Runner(opt).run();
          } catch (RunnerException e) {
            Assertions.fail(e.getMessage());
          }
        }
    );
  }

  @ParameterizedTest
  @MethodSource("dragonflyConfigs")
  public void dragonfly(String name, RedisConfig redisConfig) {
    Options opt = createBuilder(DRAGONFLY_BENCHMARKS).result(name).build();

    this.withDragonfly(
        redisConfig.toString(), () -> {
          try {
            new Runner(opt).run();
          } catch (RunnerException e) {
            Assertions.fail(e.getMessage());
          }
        }
    );
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
