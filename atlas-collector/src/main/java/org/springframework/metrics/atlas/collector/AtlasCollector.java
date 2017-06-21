package org.springframework.metrics.atlas.collector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.metrics.annotation.Timed;
import org.springframework.metrics.export.atlas.EnableAtlasMetrics;
import org.springframework.metrics.instrument.MeterRegistry;
import org.springframework.metrics.instrument.Timer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAtlasMetrics
public class AtlasCollector {
    public static void main(String[] args) {
        SpringApplication.run(AtlasCollector.class, args);
    }
}

@RestController
@Timed
class TimerController {
    @Autowired MeterRegistry registry;
    final Map<String, Timer> timers = new ConcurrentHashMap<>();

    @PostMapping("/api/timer/{name}/{timeNanos}")
    public void time(@PathVariable String name, @PathVariable Long timeNanos) {
        timers.computeIfAbsent(name, registry::timer)
            .record(timeNanos, TimeUnit.NANOSECONDS);
    }
}
