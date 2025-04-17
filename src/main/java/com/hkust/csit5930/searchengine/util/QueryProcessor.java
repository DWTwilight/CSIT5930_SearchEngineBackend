package com.hkust.csit5930.searchengine.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryProcessor {
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

    @NonNull
    public Map<String, Long> vectorizeWithNGram(@NonNull List<Token> tokens, int n) {
        var vector = new HashMap<String, Long>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < tokens.size() - i; j++) {
                if (tokens.get(j).pos() + i == tokens.get(j + i).pos()) {
                    var term = tokens.stream().skip(j).limit(i + 1).map(Token::word).collect(Collectors.joining(" "));
                    vector.compute(term, (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                }
            }
        }
        return vector;
    }

    @NonNull
    public Map<Pair<String, String>, Long> getBigrams(List<Token> tokens) {
        var bigrams = new HashMap<Pair<String, String>, Long>(); // (termAId, termBId) -> count
        for (int i = 0; i < tokens.size() - 1; i++) {
            var tokenA = tokens.get(i);
            var tokenB = tokens.get(i + 1);
            if (tokenA.pos() + 1 == tokenB.pos()) {
                bigrams.compute(Pair.of(tokenA.word(), tokenB.word()),
                        (bigram, count) -> Optional.ofNullable(count)
                                .map(c -> c + 1)
                                .orElse(1L));
            }
        }
        return bigrams;
    }

    public record Token(String word, int pos) {
    }


}
