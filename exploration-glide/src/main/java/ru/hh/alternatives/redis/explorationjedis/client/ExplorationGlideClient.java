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
            // It is recommended to set a timeout for your specific use case
            .requestTimeout(500) // 500ms timeout
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
  public void close() {
    try {
      client.close();
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
