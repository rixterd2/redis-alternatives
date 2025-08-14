package ru.hh.alternatives.redis.tests.suites;

import com.redis.testcontainers.RedisContainer;
import java.util.List;
import org.testcontainers.containers.GenericContainer;
import ru.hh.alternatives.redis.Constants;
import ru.hh.alternatives.redis.tests.benchmarks.GlideRead;
import ru.hh.alternatives.redis.tests.benchmarks.GlideWrite;
import ru.hh.alternatives.redis.tests.benchmarks.JedisRead;
import ru.hh.alternatives.redis.tests.benchmarks.JedisWrite;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceRead;
import ru.hh.alternatives.redis.tests.benchmarks.LettuceWrite;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonRead;
import ru.hh.alternatives.redis.tests.benchmarks.RedissonWrite;

public abstract class AbstractBenchmark {
  private static final GenericContainer<RedisContainer> redis = new GenericContainer<>("redis:8.0");
  private static final GenericContainer<RedisContainer> valkey = new GenericContainer<>("valkey/valkey:8.0");
  private static final GenericContainer<RedisContainer> dragonflydb = new GenericContainer<>("docker.dragonflydb.io/dragonflydb/dragonfly:latest");

  public static final List<String> REDIS_BENCHMARKS = List.of(
      JedisRead.class.getSimpleName(),
      LettuceRead.class.getSimpleName(),
      RedissonRead.class.getSimpleName(),
      JedisWrite.class.getSimpleName(),
      LettuceWrite.class.getSimpleName(),
      RedissonWrite.class.getSimpleName()
  );

  public static final List<String> VALKEY_BENCHMARKS = List.of(
      GlideRead.class.getSimpleName(),
      JedisRead.class.getSimpleName(),
      LettuceRead.class.getSimpleName(),
      RedissonRead.class.getSimpleName(),
      GlideWrite.class.getSimpleName(),
      JedisWrite.class.getSimpleName(),
      LettuceWrite.class.getSimpleName(),
      RedissonWrite.class.getSimpleName()
  );

  public static final List<String> DRAGONFLY_BENCHMARKS = List.of(
      JedisRead.class.getSimpleName(),
      LettuceRead.class.getSimpleName(),
      RedissonRead.class.getSimpleName(),
      JedisWrite.class.getSimpleName(),
      LettuceWrite.class.getSimpleName(),
      RedissonWrite.class.getSimpleName()
  );

  protected void withRedis(String config, Runnable runnable) {
    withContainer(redis, "redis-server %s".formatted(config), runnable);
  }

  protected void withValkey(String config, Runnable runnable) {
    withContainer(valkey, "valkey-server %s".formatted(config), runnable);
  }

  protected void withDragonfly(String config, Runnable runnable) {
    withContainer(dragonflydb, "dragonfly %s".formatted(config), runnable);
  }

  private static void withContainer(GenericContainer<?> container, String command, Runnable runnable) {
    container.setPortBindings(List.of("%d:%d/tcp".formatted(Constants.PORT, Constants.PORT)));
    container.setCommand(command);

    container.start();
    try {
      runnable.run();
    } finally {
      if (container.isRunning()) {
        container.stop();
      }
    }
  }

  public static class RedisConfig {
    private StringBuilder builder = new StringBuilder();

    public RedisConfig() {
    }

    private RedisConfig(StringBuilder builder) {
      this.builder = new StringBuilder(builder);
    }

    public RedisConfig withMemory(String memory) {
      builder.append("--maxmemory");
      builder.append(" ");
      builder.append(memory);
      builder.append(" ");
      return new RedisConfig(builder);
    }

    public RedisConfig withEvictionPolicy(String policy) {
      builder.append("--maxmemory-policy");
      builder.append(" ");
      builder.append(policy);
      builder.append(" ");
      return new RedisConfig(builder);
    }

    public RedisConfig onDisk() {
      builder.append("--save 3600 1000000000 --appendonly yes");
      builder.append(" ");
      return new RedisConfig(builder);
    }

    public RedisConfig inMemory() {
      builder.append("--save '' --appendonly no");
      builder.append(" ");
      return new RedisConfig(builder);
    }

    public RedisConfig withThreads(int threads) {
      builder.append("--io-threads");
      builder.append(" ");
      // main thread is not taken into account
      builder.append(threads - 1);
      builder.append(" ");
      return new RedisConfig(builder);
    }

    public String build() {
      return builder.toString();
    }

    @Override
    public String toString() {
      return build();
    }
  }

  public static class DragonflyConfig {
    private StringBuilder builder = new StringBuilder();

    public DragonflyConfig() {
    }

    private DragonflyConfig(StringBuilder builder) {
      this.builder = new StringBuilder(builder);
    }

    public DragonflyConfig withMemory(String memory) {
      builder.append("--maxmemory");
      builder.append(" ");
      builder.append(memory);
      builder.append(" ");
      return new DragonflyConfig(builder);
    }

    public DragonflyConfig withEviction() {
      builder.append("--cache_mode true");
      builder.append(" ");
      return new DragonflyConfig(builder);
    }

    public DragonflyConfig withThreads(int threads) {
      builder.append("--proactor_threads");
      builder.append(" ");
      builder.append(threads);
      builder.append(" ");
      builder.append("--conn_io_threads");
      builder.append(" ");
      builder.append(threads);
      builder.append(" ");
      return new DragonflyConfig(builder);
    }

    public String build() {
      return builder.toString();
    }

    @Override
    public String toString() {
      return build();
    }
  }
}
