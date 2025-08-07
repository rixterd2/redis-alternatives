package ru.hh.alternatives.redis;

import java.util.HashMap;
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

  public static String generateString(int sizeInBytes) {
    // char is 2 bytes in size so divide into 2
    int numChars = sizeInBytes / 2;
    StringBuilder sb = new StringBuilder(numChars);
    for (int i = 0; i < numChars; i++) {
      sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }
    return sb.toString();
  }

  public static Map<String, String> generateKeys(KeyValueClient<String, String> client, int numberOfKeys, int valueSize) {
    HashMap<String, String> generatedKeys = new HashMap<>(numberOfKeys);
    for (int i = 0; i < numberOfKeys; i++) {
      String key = UUID.randomUUID().toString();
      generatedKeys.put(key, key);
      client.set(key, Utils.generateString(valueSize));
    }
    return generatedKeys;
  }

  public static void cleanup(KeyValueClient<String, String> client, Map<String, String> data) {
    for (String key : data.keySet()) {
      client.delete(key);
    }
  }
}
