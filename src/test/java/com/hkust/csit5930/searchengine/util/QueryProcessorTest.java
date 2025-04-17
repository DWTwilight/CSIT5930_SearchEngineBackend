package com.hkust.csit5930.searchengine.util;

import static org.junit.jupiter.api.Assertions.*;

import com.hkust.csit5930.searchengine.config.StopWordsConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;

import java.util.List;

@SpringBootTest(classes = {QueryProcessor.class, StopWordsConfiguration.class})
class QueryProcessorTest {

    @Autowired
    private QueryProcessor queryProcessor;

    @Test
    void should_tokenize_with_position() {
        List<QueryProcessor.Token> result = queryProcessor.tokenize("the quick brown foxes' jumps");

        assertAll(
                () -> assertEquals(4, result.size()),
                () -> assertEquals("quick", result.get(0).word()),
                () -> assertEquals(1, result.get(0).pos()),
                () -> assertEquals("fox", result.get(2).word())
        );
    }

    @ParameterizedTest
    @MethodSource("tokenizeCases")
    void parameterized_tokenize_test(String input, List<String> expectedWords) {
        List<QueryProcessor.Token> tokens = queryProcessor.tokenize(input);
        assertIterableEquals(expectedWords, tokens.stream().map(QueryProcessor.Token::word).toList());
    }

    private static List<Arguments> tokenizeCases() {
        return List.of(
                Arguments.of("Test!ing 123", List.of("test", "ing", "123")),
                Arguments.of("Hello@WORLD", List.of("hello", "world")),
                Arguments.of("empty''''string", List.of("empti", "string"))
        );
    }

    @Test
    void should_handle_all_stopwords() {
        assertEquals(0, queryProcessor.tokenize("a an the").size());
    }

    @Test
    void should_keep_numbers_and_special_chars() {
        List<QueryProcessor.Token> tokens = queryProcessor.tokenize("123@test");
        assertEquals("123", tokens.get(0).word());
    }

    @Test
    void should_vectorize_tokens() {
        List<QueryProcessor.Token> tokens = queryProcessor.tokenize("this is a vectorize test, let's see the test results hong kong");
        var vector = queryProcessor.vectorizeWithNGram(tokens, 4);
        assertEquals(12, vector.size());
        assertEquals(2, vector.get("test"));
    }

    @Test
    void should_get_bigrams() {
        List<QueryProcessor.Token> tokens = queryProcessor.tokenize("this is a vectorize test, let's see the test results, and a vectorize test");
        var bigrams = queryProcessor.getBigrams(tokens);
        assertEquals(2, bigrams.size());
        assertEquals(2, bigrams.get(Pair.of("vector", "test")));
    }
}