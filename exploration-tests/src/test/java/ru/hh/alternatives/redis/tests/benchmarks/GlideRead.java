package ru.hh.alternatives.redis.tests.benchmarks;

import java.util.concurrent.ConcurrentHashMap;
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
@State(Scope.Benchmark)
public class GlideRead {
  private static final KeyValueClient<String, String> glide = new ExplorationGlideClient(Constants.HOST, Constants.PORT);
  private static final ConcurrentHashMap<String, String> KEYS = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, String> KEYS_1MB = new ConcurrentHashMap<>();

  @Setup(Level.Trial)
  public static void setup() {
    KEYS.putAll(Utils.generateKeys(glide, 16, Constants.KB_1)); // 1kb
    KEYS_1MB.putAll(Utils.generateKeys(glide, 16, Constants.MB_1)); // 1mb
  }

  @TearDown(Level.Trial)
  public static void tearDown() {
    Utils.cleanup(glide, KEYS);
    Utils.cleanup(glide, KEYS_1MB);
    glide.close();
  }

  @Benchmark
  public void get() {
    glide.get(Utils.randomKey(KEYS));
  }

  @Benchmark
  public void get1Mb() {
    glide.get(Utils.randomKey(KEYS_1MB));
  }
}
