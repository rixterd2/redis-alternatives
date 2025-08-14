package ru.hh.alternatives.redis.tests.suites;

import java.util.List;
import java.util.UUID;
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
import ru.hh.alternatives.redis.Constants;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;
import ru.hh.alternatives.redis.explorationjedis.client.JedisClient;

public class MemoryPressureTest extends AbstractBenchmark {
  private static final long CACHE_SIZE = 2;

  private static Stream<Arguments> redisConfigs() {
    String container = "redis";
    RedisConfig diskRedisConfig = new RedisConfig().onDisk();
    RedisConfig memoryRedisConfig = new RedisConfig().inMemory();
    return Stream.of(
        Arguments.of(
            "%s-%s-disk-lru".formatted(MemoryPressureTest.class.getSimpleName(), container),
            diskRedisConfig.withMemory("%dg".formatted(CACHE_SIZE)).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lfu".formatted(MemoryPressureTest.class.getSimpleName(), container),
            diskRedisConfig.withMemory("%dg".formatted(CACHE_SIZE)).withEvictionPolicy("allkeys-lfu")
        ),
        Arguments.of(
            "%s-%s-inmemory-lru".formatted(MemoryPressureTest.class.getSimpleName(), container),
            memoryRedisConfig.withMemory("%dg".formatted(CACHE_SIZE)).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-inmemory-lfu".formatted(MemoryPressureTest.class.getSimpleName(), container),
            memoryRedisConfig.withMemory("%dg".formatted(CACHE_SIZE)).withEvictionPolicy("allkeys-lfu")
        )
    );
  }

  private static Stream<Arguments> valkeyConfigs() {
    String container = "valkey";
    RedisConfig diskRedisConfig = new RedisConfig().onDisk();
    RedisConfig memoryRedisConfig = new RedisConfig().inMemory();
    return Stream.of(
        Arguments.of(
            "%s-%s-disk-lru".formatted(MemoryPressureTest.class.getSimpleName(), container),
            diskRedisConfig.withMemory("%dg".formatted(CACHE_SIZE)).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-disk-lfu".formatted(MemoryPressureTest.class.getSimpleName(), container),
            diskRedisConfig.withMemory("%dg".formatted(CACHE_SIZE)).withEvictionPolicy("allkeys-lfu")
        ),
        Arguments.of(
            "%s-%s-inmemory-lru".formatted(MemoryPressureTest.class.getSimpleName(), container),
            memoryRedisConfig.withMemory("%dg".formatted(CACHE_SIZE)).withEvictionPolicy("allkeys-lru")
        ),
        Arguments.of(
            "%s-%s-inmemory-lfu".formatted(MemoryPressureTest.class.getSimpleName(), container),
            memoryRedisConfig.withMemory("%dg".formatted(CACHE_SIZE)).withEvictionPolicy("allkeys-lfu")
        )
    );
  }

  private static Stream<Arguments> dragonflyConfigs() {
    DragonflyConfig diskRedisConfig = new DragonflyConfig();
    return Stream.of(
        Arguments.of(
            "%s-dragonfly-disk-eviction".formatted(MemoryPressureTest.class.getSimpleName()),
            diskRedisConfig.withMemory("%dg".formatted(CACHE_SIZE)).withEviction()
        ),
        Arguments.of(
            "%s-dragonfly-disk-noeviction".formatted(MemoryPressureTest.class.getSimpleName()),
            diskRedisConfig.withMemory("%dg".formatted(CACHE_SIZE))
        )
    );
  }

  @ParameterizedTest
  @MethodSource("redisConfigs")
  public void redis(String name, RedisConfig redisConfig) {
    Options opt = createBuilder(REDIS_BENCHMARKS).result(name).build();

    this.withRedis(
        redisConfig.toString(), () -> {
          setupMemoryPressure();
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
          setupMemoryPressure();
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
  public void dragonfly(String name, DragonflyConfig dragonflyConfig) {
    Options opt = createBuilder(DRAGONFLY_BENCHMARKS).result(name).build();

    this.withDragonfly(
        dragonflyConfig.toString(), () -> {
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

  private void setupMemoryPressure() {
    KeyValueClient<String, String> jedis = new JedisClient(Constants.HOST, Constants.PORT);
    // 2 just in case to fill up the memory
    long keyCount = CACHE_SIZE * 1024 * 2;
    for (int i = 0; i < keyCount; i++) {
      jedis.set(UUID.randomUUID().toString(), Constants.VALUE_1MB);
    }
    jedis.close();
  }
}
