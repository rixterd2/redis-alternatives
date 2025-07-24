package ru.hh.alternatives.redis;

import java.util.Random;

public class Utils {
  private static final Random RANDOM = new Random();
  // Use characters that are 1 byte in UTF-8 for clean size
  private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  public static String randomString1Mb() {
    // char is 2 bytes in size so divide into 2
    int numChars = 1024 * 1024 / 2;
    StringBuilder sb = new StringBuilder(numChars);
    for (int i = 0; i < numChars; i++) {
      sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}
