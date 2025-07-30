package ru.hh.alternatives.redis.explorationjedis;

public interface KeyValueClient<K,V>{
  V get(K key);

  void set(K key, V value);

  void setAndExpire(K key, V value, long seconds);

  void delete(K key);

  void close();
}
