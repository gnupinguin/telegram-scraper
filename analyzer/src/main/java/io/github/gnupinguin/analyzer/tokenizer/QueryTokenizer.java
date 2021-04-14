package io.github.gnupinguin.analyzer.tokenizer;

import io.github.gnupinguin.analyzer.entity.Query;

import javax.annotation.Nonnull;

public interface QueryTokenizer extends Tokenizer<Query> {
    @Nonnull
    TokenizedEntity<Query> map(@Nonnull Query query);
}
