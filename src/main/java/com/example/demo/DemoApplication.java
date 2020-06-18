package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        Thread.sleep(100_000); //Sleep for 100 seconds
        return "Task";
    }

    @Bean
    public GracefulShutdown gracefulShutdown() {
        return new GracefulShutdown();
    }

    @Bean
    public GracefullShutdownTomcatWebServerCustomizer myTomcatWebServerCustomizer() {
        return new GracefullShutdownTomcatWebServerCustomizer(gracefulShutdown());
    }

    @RequiredArgsConstructor
    private static class GracefullShutdownTomcatWebServerCustomizer
            implements WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> {

        private final TomcatConnectorCustomizer tomcatConnectorCustomizer;

        @Override
        public void customize(ConfigurableTomcatWebServerFactory factory) {
            factory.addConnectorCustomizers(tomcatConnectorCustomizer);
        }
    }

    private static class GracefulShutdown implements TomcatConnectorCustomizer {

        private volatile Connector connector;

        @Override
        public void customize(Connector connector) {
            this.connector = connector;
        }

        @EventListener(ContextClosedEvent.class)
        public void onApplicationEvent() {
            log.info("Preparing web server shutdown");
            this.connector.pause();
            Executor executor = this.connector.getProtocolHandler().getExecutor();
            if (executor instanceof ThreadPoolExecutor) {
                try {
                    log.info("Shutting down all threads");
                    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                    log.info("Active threads: {}", threadPoolExecutor.getActiveCount());
                    threadPoolExecutor.shutdown();
                    if (!threadPoolExecutor.awaitTermination(120, TimeUnit.SECONDS)) {
                        log.warn("Tomcat thread pool did not shut down gracefully within "
                                + "120 seconds. Proceeding with forceful shutdown");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

    }

}
