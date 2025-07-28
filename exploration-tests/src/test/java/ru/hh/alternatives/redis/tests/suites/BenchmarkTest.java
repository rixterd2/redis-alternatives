package ru.hh.alternatives.redis.tests.suites;

import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.hh.alternatives.redis.Constants;
import ru.hh.alternatives.redis.Utils;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;
import ru.hh.alternatives.redis.explorationjedis.client.JedisClient;
import ru.hh.alternatives.redis.tests.benchmarks.Jedis;
import ru.hh.alternatives.redis.tests.benchmarks.Lettuce;
import ru.hh.alternatives.redis.tests.benchmarks.Redisson;

public class BenchmarkTest {

  @BeforeAll
  public static void beforeAll() {
    KeyValueClient<String, String> jedis = new JedisClient(Constants.HOST, Constants.PORT);
    for (int i = 0; i < 128; i++) {
      String key = UUID.randomUUID().toString();
      Constants.KEYS_1MB.put(key, key);
      jedis.set(key, Utils.randomString1Mb());
    }

    for (int i = 0; i < 128; i++) {
      String key = UUID.randomUUID().toString();
      Constants.KEYS.put(key, key);
      jedis.set(key, UUID.randomUUID().toString());
    }

    jedis.close();
  }

  @Test
  public void memoryPressure() throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(Jedis.class.getSimpleName())
        .include(Lettuce.class.getSimpleName())
        .include(Redisson.class.getSimpleName())
        .warmupIterations(5)
        .measurementIterations(10)
        .forks(1)
        .build();
    new Runner(opt).run();
  }
}
