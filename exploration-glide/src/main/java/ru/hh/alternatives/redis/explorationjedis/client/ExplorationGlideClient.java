package ru.hh.alternatives.redis.explorationjedis.client;

import glide.api.GlideClient;
import glide.api.models.configuration.GlideClientConfiguration;
import glide.api.models.configuration.NodeAddress;
import java.util.concurrent.ExecutionException;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;

public class ExplorationGlideClient implements KeyValueClient<String, String> {
  private final GlideClient client;

  public ExplorationGlideClient(String host, int port) {
    GlideClientConfiguration config =
        GlideClientConfiguration.builder()
            .address(NodeAddress.builder().host(host).port(port).build())
            .useTLS(false)
            .requestTimeout(1000)
            .build();
    try {
      client = GlideClient.createClient(config).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String get(String key) {
    try {
      return client.get(key).get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void set(String key, String value) {
    try {
      client.set(key, value).get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setAndExpire(String key, String value, long seconds) {
    try {
      client.set(key, value).get();
      client.expire(key, seconds);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(String key) {
    try {
      String[] keyHolder = new String[]{key};
      client.del(keyHolder).get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      client.close();
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
