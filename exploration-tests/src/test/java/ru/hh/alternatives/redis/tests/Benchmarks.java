package ru.hh.alternatives.redis.tests;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;
import ru.hh.alternatives.redis.explorationjedis.client.JedisClient;
import ru.hh.alternatives.redis.explorationlettuce.client.LettuceClient;
import ru.hh.alternatives.redis.explorationredisson.client.ExplorationRedissonClient;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class Benchmarks {
  private static final String HOST = "localhost";
  private static final int PORT = 6379;

  private static final KeyValueClient<String, String> jedis = new JedisClient(HOST, PORT);
  private static final KeyValueClient<String, String> lettuce = new LettuceClient(HOST, PORT);
  private static final KeyValueClient<String, String> redisson = new ExplorationRedissonClient(HOST, PORT);

  @Benchmark
  public void testJedisSet() {
    jedis.set(UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  @Benchmark
  public void testJedisGet() {
    jedis.get(UUID.randomUUID().toString());
  }

  @Benchmark
  public void testLettuceSet() {
    lettuce.set(UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  @Benchmark
  public void testLettuceGet() {
    lettuce.get(UUID.randomUUID().toString());
  }

  @Benchmark
  public void testRedissonSet() {
    redisson.set(UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  @Benchmark
  public void testRedissonGet() {
    redisson.get(UUID.randomUUID().toString());
  }

  public static void main(String[] args) throws Exception {
    Options opt = new OptionsBuilder()
        .include(Benchmarks.class.getSimpleName())
        .warmupIterations(5)
        .measurementIterations(10)
        .forks(1)
        .build();

    new Runner(opt).run();

    jedis.close();
    lettuce.close();
    redisson.close();
  }
}
