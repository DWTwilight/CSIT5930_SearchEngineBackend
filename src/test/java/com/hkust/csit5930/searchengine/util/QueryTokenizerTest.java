package com.hkust.csit5930.searchengine.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

@SpringBootTest(classes = {QueryTokenizer.class, StopWordsConfiguration.class})
class QueryTokenizerTest {

    @Autowired
    private QueryTokenizer tokenizer;

    @Test
    void should_tokenize_with_position() {
        List<QueryTokenizer.Token> result = tokenizer.tokenize("the quick brown foxes' jumps");

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
        List<QueryTokenizer.Token> tokens = tokenizer.tokenize(input);
        assertIterableEquals(expectedWords, tokens.stream().map(QueryTokenizer.Token::word).toList());
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
        assertEquals(0, tokenizer.tokenize("a an the").size());
    }

    @Test
    void should_keep_numbers_and_special_chars() {
        List<QueryTokenizer.Token> tokens = tokenizer.tokenize("123@test");
        assertEquals("123", tokens.get(0).word());
    }
}