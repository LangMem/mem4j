package com.mem0;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot application class for Java Mem0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.mem0")
public class Mem0Application {

  public static void main(String[] args) {
    SpringApplication.run(Mem0Application.class, args);
  }
}