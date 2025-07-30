package ru.hh.alternatives.redis.tests.benchmarks;

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
public class GlideRead {
  private static final KeyValueClient<String, String> glide = new ExplorationGlideClient(Constants.HOST, Constants.PORT);

  @Setup(Level.Trial)
  public static void setup() {
    Utils.setupKeys(glide);
  }

  @TearDown(Level.Trial)
  public static void tearDown() {
    Utils.cleanupKeys(glide);
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
}
