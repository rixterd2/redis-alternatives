package ru.hh.alternatives.redis.tests.benchmarks;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
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

  @Setup(Level.Iteration)
  public void setupIter() {
    Utils.setupKeys(jedis);
  }

  @TearDown(Level.Iteration)
  public void tearDownIter() {
    Utils.cleanupKeys(jedis);
  }

  @TearDown(Level.Trial)
  public static void tearDown() {
    jedis.close();
  }

  @Benchmark
  public void get() {
    jedis.get(Utils.randomKey(Constants.KEYS));
  }

  @Benchmark
  public void get1Mb() {
    jedis.get(Utils.randomKey(Constants.KEYS_1MB));
  }

  @Benchmark
  public void set() {
    String key = UUID.randomUUID().toString();
    jedis.set(key, UUID.randomUUID().toString());
    Constants.KEYS_TO_REMOVE.put(key, key);
  }

  @Benchmark
  public void set1Mb() {
    String key = UUID.randomUUID().toString();
    jedis.set(key, Utils.randomString1Mb());
    Constants.KEYS_TO_REMOVE.put(key, key);
  }
}
