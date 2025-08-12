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
import ru.hh.alternatives.redis.explorationjedis.client.ExplorationGlideClient;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class GlideWrite {
  private static final KeyValueClient<String, String> glide = new ExplorationGlideClient(Constants.HOST, Constants.PORT);

  @TearDown(Level.Trial)
  public static void tearDown() {
    glide.close();
  }

  @Benchmark
  public void setAndExpire() {
    String key = UUID.randomUUID().toString();
    glide.setAndExpire(key, Constants.VALUE_1KB, 10);
  }

  @Benchmark
  public void set1MbAndExpire() {
    String key = UUID.randomUUID().toString();
    glide.setAndExpire(key, Constants.VALUE_1MB, 10);
  }

  @Benchmark
  public void setAndDelete() {
    String key = UUID.randomUUID().toString();
    glide.set(key, Constants.VALUE_1KB);
    glide.delete(key);
  }

  @Benchmark
  public void set1MbAndDelete() {
    String key = UUID.randomUUID().toString();
    glide.set(key, Constants.VALUE_1MB);
    glide.delete(key);
  }
}
