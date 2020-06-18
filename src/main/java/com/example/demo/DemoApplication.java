package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@SpringBootApplication
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/get-task")
    public String getTask() throws InterruptedException {
        log.info("Getting task");
        int initialValue = 100;
        AtomicInteger count = new AtomicInteger(initialValue);

        for (int i = 0; i < initialValue; i++) {
            count.decrementAndGet();
            log.info("{} seconds left to process request", count.get());
            Thread.sleep(1000);
        }

        return "Task";
    }

}
