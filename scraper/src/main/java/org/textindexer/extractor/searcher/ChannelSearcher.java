package org.textindexer.extractor.searcher;

import io.github.gnupinguin.tlgscraper.model.db.Chat;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface ChannelSearcher {

    Optional<Chat> searchChannel(@Nonnull String name);

}
