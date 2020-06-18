package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@ConditionalOnBean(GracefulShutdownConfiguration.class)
public class ShutdownTomcatConnectorCustomizer implements TomcatConnectorCustomizer {

    @Value("${spring.graceful-shutdown.interval:60}")
    private int gracefulShutdownTimeout;

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
                if (!threadPoolExecutor.awaitTermination(gracefulShutdownTimeout, TimeUnit.SECONDS)) {
                    log.warn("Tomcat thread pool did not shut down gracefully within {} seconds." +
                            " Proceeding with forceful shutdown", gracefulShutdownTimeout);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
