package com.goodmaps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configuration du WebClient Spring avec gestion de tampon étendue pour les requêtes Overpass volumineuses,
 * timeouts de connexion et en-tête User-Agent conforme aux règles OSM.
 */
@Configuration
public class WebClientConfig {

    @Value("${goodmaps.osm.user-agent:GoodMapsAccessibilityPlatform/1.0 (contact: support@goodmaps.local)}")
    private String userAgent;

    @Value("${goodmaps.osm.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${goodmaps.osm.read-timeout-ms:15000}")
    private int readTimeoutMs;

    @Bean
    public WebClient osmWebClient() {
        // Tampon mémoire élargi à 16 MB pour recevoir de gros graphes de POIs OSM sans BufferOverflow
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeoutMs));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }
}
