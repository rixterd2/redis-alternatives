package ru.hh.alternatives.redis;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import ru.hh.alternatives.redis.explorationjedis.KeyValueClient;

public class Utils {
  private static final Random RANDOM = new Random();
  // Use characters that are 1 byte in UTF-8 for clean size
  private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  public static String randomKey(Map<String, String> map) {
    int size = map.size();
    return map.keySet().stream().skip(RANDOM.nextInt(size)).findFirst().get();
  }

  public static String randomString1Mb() {
    // char is 2 bytes in size so divide into 2
    int numChars = 1024 * 1024 / 2;
    StringBuilder sb = new StringBuilder(numChars);
    for (int i = 0; i < numChars; i++) {
      sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }
    return sb.toString();
  }

  public static void setupKeys(KeyValueClient<String, String> client) {
    for (int i = 0; i < 128; i++) {
      String key = UUID.randomUUID().toString();
      Constants.KEYS_1MB.put(key, key);
      client.set(key, Utils.randomString1Mb());
    }

    for (int i = 0; i < 128; i++) {
      String key = UUID.randomUUID().toString();
      Constants.KEYS.put(key, key);
      client.set(key, UUID.randomUUID().toString());
    }
  }

  public static void cleanupKeys(KeyValueClient<String, String> client) {
    for (String key : Constants.KEYS.keySet()) {
      client.delete(key);
    }

    for (String key : Constants.KEYS_1MB.keySet()) {
      client.delete(key);
    }

    for (String key : Constants.KEYS_TO_REMOVE.keySet()) {
      client.delete(key);
    }
  }
}
