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
public class RedissonWrite {
  private static final KeyValueClient<String, String> redisson = new ExplorationRedissonClient(Constants.HOST, Constants.PORT);

  @TearDown(Level.Trial)
  public static void tearDown() {
    redisson.close();
  }

  @Benchmark
  public void setAndExpire() {
    String key = UUID.randomUUID().toString();
    redisson.setAndExpire(key, UUID.randomUUID().toString(), 10);
  }

  @Benchmark
  public void set1MbAndExpire() {
    String key = UUID.randomUUID().toString();
    redisson.setAndExpire(key, Utils.randomString1Mb(), 10);
  }

  @Benchmark
  public void setAndDelete() {
    String key = UUID.randomUUID().toString();
    redisson.set(key, UUID.randomUUID().toString());
    redisson.delete(key);
  }

  @Benchmark
  public void set1MbAndDelete() {
    String key = UUID.randomUUID().toString();
    redisson.set(key, Utils.randomString1Mb());
    redisson.delete(key);
  }
}
