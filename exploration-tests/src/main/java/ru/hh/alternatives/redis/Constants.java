package ru.hh.alternatives.redis;

public class Constants {
  public static final String HOST = "localhost";
  public static final int PORT = 6379;
  public static final int KB_1 = 1024;
  public static final String VALUE_1KB = Utils.generateString(KB_1);
  public static final int MB_1 = 1024 * 1024;
  public static final String VALUE_1MB = Utils.generateString(MB_1);
}
