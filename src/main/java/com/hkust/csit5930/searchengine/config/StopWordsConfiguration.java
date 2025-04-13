package com.hkust.csit5930.searchengine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
public class StopWordsConfiguration {
    @Bean
    public Set<String> stopWords() throws IOException {
        final String STOP_WORDS_FILE = "stopwords.txt";
        Set<String> stopWords = new HashSet<>();

        try (var reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(STOP_WORDS_FILE).getInputStream(),
                        StandardCharsets.UTF_8
                )
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    stopWords.add(trimmed.toLowerCase());
                }
            }
        }
        log.debug("StopWords: {}", stopWords);
        return Set.copyOf(stopWords);
    }
}
