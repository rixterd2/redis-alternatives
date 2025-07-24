package ru.hh.alternatives.redis.tests.benchmarks;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import ru.hh.alternatives.redis.Constants;
import ru.hh.alternatives.redis.Utils;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;
import ru.hh.alternatives.redis.explorationjedis.client.JedisClient;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class Jedis {
  private static final KeyValueClient<String, String> jedis = new JedisClient(Constants.HOST, Constants.PORT);

  @TearDown(Level.Trial)
  public static void tearDown() {
    jedis.close();
  }

  @Benchmark
  public void set() {
    jedis.set(UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  @Benchmark
  public void get() {
    jedis.get(UUID.randomUUID().toString());
  }

  @Benchmark
  public void set1Mb() {
    jedis.set(UUID.randomUUID().toString(), Utils.randomString1Mb());
  }
}
