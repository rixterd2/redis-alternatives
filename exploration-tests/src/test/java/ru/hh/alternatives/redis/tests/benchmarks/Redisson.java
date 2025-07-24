package ru.hh.alternatives.redis.tests.benchmarks;

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
import ru.hh.alternatives.redis.explorationredisson.client.ExplorationRedissonClient;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class Redisson {
  private static final KeyValueClient<String, String> redisson = new ExplorationRedissonClient(Constants.HOST, Constants.PORT);

  @TearDown(Level.Trial)
  public static void tearDown() {
    redisson.close();
  }

  @Benchmark
  public void set() {
    redisson.set(UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  @Benchmark
  public void get() {
    redisson.get(UUID.randomUUID().toString());
  }

  @Benchmark
  public void set1Mb() {
    redisson.set(UUID.randomUUID().toString(), Utils.randomString1Mb());
  }
}
