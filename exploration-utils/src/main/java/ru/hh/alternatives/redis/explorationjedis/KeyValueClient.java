package ru.hh.alternatives.redis.explorationjedis;

public interface KeyValueClient<K,V>{
  V get(K key);

  void set(K key, V value);

  void delete(K key);

  void close();
}
