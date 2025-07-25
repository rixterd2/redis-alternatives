package ru.hh.alternatives.redis.tests.suites;

import com.redis.testcontainers.RedisContainer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.hh.alternatives.redis.Constants;
import ru.hh.alternatives.redis.tests.benchmarks.Glide;
import ru.hh.alternatives.redis.tests.benchmarks.Jedis;
import ru.hh.alternatives.redis.tests.benchmarks.Lettuce;
import ru.hh.alternatives.redis.tests.benchmarks.Redisson;

public class BenchmarkTest {
  private static final RedisContainer REDIS_CONTAINER = new RedisContainer("redis:8.0");
  private static final RedisContainer VALKEY_CONTAINER = new RedisContainer("valkey/valkey:8.0");

  @Test
  public void redisBenchmark() throws RunnerException {
    REDIS_CONTAINER.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));

    try {
      REDIS_CONTAINER.start();
      Options opt = new OptionsBuilder()
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(1)
          .build();
      new Runner(opt).run();
    } finally {
      if (REDIS_CONTAINER.isRunning()) {
        REDIS_CONTAINER.stop();
      }
    }
  }

  @Test
  public void valkeyBenchmark() throws RunnerException {
    VALKEY_CONTAINER.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));

    try {
      VALKEY_CONTAINER.start();
      Options opt = new OptionsBuilder()
          .include(Glide.class.getSimpleName())
          .include(Jedis.class.getSimpleName())
          .include(Lettuce.class.getSimpleName())
          .include(Redisson.class.getSimpleName())
          .warmupIterations(5)
          .measurementIterations(10)
          .forks(1)
          .build();
      new Runner(opt).run();
    } finally {
      if (VALKEY_CONTAINER.isRunning()) {
        VALKEY_CONTAINER.stop();
      }
    }
  }
}
