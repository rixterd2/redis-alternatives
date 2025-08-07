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
import ru.hh.alternatives.redis.explorationlettuce.client.LettuceClient;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class LettuceRead {
  private static final KeyValueClient<String, String> lettuce = new LettuceClient(Constants.HOST, Constants.PORT);
  private static final ConcurrentHashMap<String, String> KEYS = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, String> KEYS_1MB = new ConcurrentHashMap<>();

  @Setup(Level.Trial)
  public static void setup() {
    KEYS.putAll(Utils.generateKeys(lettuce, 16, Constants.KB_1));
    KEYS_1MB.putAll(Utils.generateKeys(lettuce, 16, Constants.MB_1));
  }

  @TearDown(Level.Trial)
  public static void tearDown() {
    Utils.cleanup(lettuce, KEYS);
    Utils.cleanup(lettuce, KEYS_1MB);
    lettuce.close();
  }

  @Benchmark
  public void get() {
    lettuce.get(Utils.randomKey(KEYS));
  }

  @Benchmark
  public void get1Mb() {
    lettuce.get(Utils.randomKey(KEYS_1MB));
  }
}
