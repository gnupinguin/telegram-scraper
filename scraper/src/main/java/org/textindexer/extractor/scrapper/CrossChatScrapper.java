package org.textindexer.extractor.scrapper;

import javax.annotation.Nonnull;
import java.util.List;

public interface CrossChatScrapper {

    void deepScrap(@Nonnull List<String> chatNames);

    void plainScrap(@Nonnull List<String> chatNames);

}
