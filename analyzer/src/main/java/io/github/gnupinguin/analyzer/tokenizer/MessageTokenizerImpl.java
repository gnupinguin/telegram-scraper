package io.github.gnupinguin.analyzer.tokenizer;

import io.github.gnupinguin.analyzer.document.Document;
import io.github.gnupinguin.analyzer.entity.Msg;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class MessageTokenizerImpl extends AbstractPipeline<Document<Msg>> {

    private Class<Document<Msg>> entityType = (Class<Document<Msg>>)(new Document<Msg>(null, null, null).getClass());

    public MessageTokenizerImpl() {
        super("text_content");
    }


    @Nonnull
    @Override
    protected Class<Document<Msg>> getEntityClass() {
        return entityType;
    }
}
