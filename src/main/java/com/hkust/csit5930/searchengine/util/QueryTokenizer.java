package com.hkust.csit5930.searchengine.util;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class QueryTokenizer {
    private final Set<String> stopWords;
    private final PorterStemmer stem = new PorterStemmer();

    @NonNull
    public List<Token> tokenize(@NonNull String query) {
        var rawTokenList = Arrays.stream(query.trim().toLowerCase().split("[^a-z0-9]+")).filter(token -> !token.isBlank()).toList();
        var tokens = new ArrayList<Token>(rawTokenList.size());

        // stop word removal & stemming
        for (int i = 0; i < rawTokenList.size(); i++) {
            String rawToken = rawTokenList.get(i);
            if (!stopWords.contains(rawToken)) {
                stem.setCurrent(rawToken);
                stem.stem();
                tokens.add(new Token(stem.getCurrent(), i));
            }
        }

        return tokens;
    }

    public record Token(String word, int pos) {
    }


}
