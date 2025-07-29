package ru.hh.alternatives.redis.tests.suites;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.hh.alternatives.redis.tests.benchmarks.Jedis;
import ru.hh.alternatives.redis.tests.benchmarks.Lettuce;
import ru.hh.alternatives.redis.tests.benchmarks.Redisson;

public class BenchmarkTest {

  @Test
  public void memoryPressure() throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(Jedis.class.getSimpleName())
        .include(Lettuce.class.getSimpleName())
        .include(Redisson.class.getSimpleName())
        .warmupIterations(5)
        .measurementIterations(10)
        .forks(1)
        .build();
    new Runner(opt).run();
  }
}
