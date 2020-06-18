package com.example.demo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring", name = "graceful-shutdown.enabled", havingValue = "true")
public class GracefulShutdownConfiguration {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> tomcatWebServerCustomizer(
            ShutdownTomcatConnectorCustomizer tomcatConnectorCustomizer) {
        return factory -> factory.addConnectorCustomizers(tomcatConnectorCustomizer);
    }

}

