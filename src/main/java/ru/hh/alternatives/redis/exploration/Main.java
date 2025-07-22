package ru.hh.alternatives.redis.exploration;

import org.springframework.boot.SpringApplication;
import ru.hh.common.spring.boot.autoconfigure.HhSpringBootApplication;
import ru.hh.common.spring.boot.profile.MainProfile;

@HhSpringBootApplication
@MainProfile
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

}
