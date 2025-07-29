package ru.hh.alternatives.redis;

import java.util.concurrent.ConcurrentHashMap;

public class Constants {
  public static final String HOST = "localhost";
  public static final int PORT = 6379;
  public static final ConcurrentHashMap<String, String> KEYS = new ConcurrentHashMap<>();
  public static final ConcurrentHashMap<String, String> KEYS_1MB = new ConcurrentHashMap<>();
  public static final ConcurrentHashMap<String, String> KEYS_TO_REMOVE = new ConcurrentHashMap<>();
}
