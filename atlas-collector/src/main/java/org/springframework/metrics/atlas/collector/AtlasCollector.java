package org.springframework.metrics.atlas.collector;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class AtlasCollector {
  public static void main(String[] args) {
    SpringApplication.run(AtlasCollector.class, args);
  }
}

@RestController
class TimerController {
  @Autowired
  MeterRegistry registry;

  @PostMapping("/api/timer/{name}/{timeNanos}")
  public void time(@PathVariable String name, @PathVariable Long timeNanos) {
    registry.timer(name).record(timeNanos, TimeUnit.NANOSECONDS);
  }
}
