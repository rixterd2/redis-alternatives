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
import ru.hh.alternatives.redis.explorationredisson.client.ExplorationRedissonClient;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class RedissonRead {
  private static final KeyValueClient<String, String> redisson = new ExplorationRedissonClient(Constants.HOST, Constants.PORT);
  private static final ConcurrentHashMap<String, String> KEYS = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, String> KEYS_1MB = new ConcurrentHashMap<>();

  @Setup(Level.Trial)
  public static void setup() {
    KEYS.putAll(Utils.generateKeys(redisson, 16, Constants.KB_1));
    KEYS_1MB.putAll(Utils.generateKeys(redisson, 16, Constants.MB_1));
  }

  @TearDown(Level.Trial)
  public static void tearDown() {
    Utils.cleanup(redisson, KEYS);
    Utils.cleanup(redisson, KEYS_1MB);
    redisson.close();
  }

  @Benchmark
  public void get() {
    redisson.get(Utils.randomKey(KEYS));
  }

  @Benchmark
  public void get1Mb() {
    redisson.get(Utils.randomKey(KEYS_1MB));
  }
}
