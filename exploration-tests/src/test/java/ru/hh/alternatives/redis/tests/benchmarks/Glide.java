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
import ru.hh.alternatives.redis.explorationjedis.client.ExplorationGlideClient;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class Glide {
  private static final KeyValueClient<String, String> glide = new ExplorationGlideClient(Constants.HOST, Constants.PORT);

  @Setup(Level.Iteration)
  public void setupIter() {
    Utils.setupKeys(glide);
  }

  @TearDown(Level.Iteration)
  public void tearDownIter() {
    Utils.cleanupKeys(glide);
  }

  @TearDown(Level.Trial)
  public static void tearDown() {
    glide.close();
  }

  @Benchmark
  public void get() {
    glide.get(Utils.randomKey(Constants.KEYS));
  }

  @Benchmark
  public void get1Mb() {
    glide.get(Utils.randomKey(Constants.KEYS_1MB));
  }

  @Benchmark
  public void set() {
    String key = UUID.randomUUID().toString();
    glide.set(key, UUID.randomUUID().toString());
    Constants.KEYS_TO_REMOVE.put(key, key);
  }

  @Benchmark
  public void set1Mb() {
    String key = UUID.randomUUID().toString();
    glide.set(key, Utils.randomString1Mb());
    Constants.KEYS_TO_REMOVE.put(key, key);
  }
}
