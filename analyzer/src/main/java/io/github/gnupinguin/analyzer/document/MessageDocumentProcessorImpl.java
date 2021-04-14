package io.github.gnupinguin.analyzer.document;

import io.github.gnupinguin.analyzer.entity.Msg;
import io.github.gnupinguin.analyzer.tokenizer.AbstractPipeline;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class MessageDocumentProcessorImpl extends AbstractPipeline<Document<Msg>> implements MessageDocumentProcessor {

    public static final Class<Document<Msg>> CLASS = (Class<Document<Msg>>) (new Document<Msg>(null, null, null).getClass());
//    private final Pipeline rankPipeline;
    private final CountVectorizer vectorizer;
    private final IDF idf;
    private final HashingTF hashingTF;

    public MessageDocumentProcessorImpl() {
        super("text_content");

        vectorizer = new CountVectorizer()
                .setInputCol(cleanerOutput())
                .setOutputCol("rawFeatures");

        hashingTF = new HashingTF()
                .setInputCol(cleanerOutput())
                .setOutputCol("rawFeatures");

        idf = new IDF()
                .setInputCol("rawFeatures")
                .setOutputCol("features")
        ;
    }

    @Override
    public DocumentCollection<Msg, Document<Msg>> map(Dataset<Row> data) {
        Dataset<Row> tokenized = basePipeline.fit(data).transform(data);

//        CountVectorizerModel vectorizerModel = vectorizer.fit(tokenized);
//        Dataset<Row> vectorized = vectorizerModel.transform(tokenized);

        Dataset<Row> rawFeaturized = hashingTF.transform(tokenized);

//        rawFeaturized.select(col(), col("rawFeatures"), )

        IDFModel idfModel = idf.fit(rawFeaturized);
        Dataset<Row> idfResult = idfModel.transform(rawFeaturized);

        Dataset<Document<Msg>> features = mapRows(idfResult, r -> new Document<>(r.getAs("features"),
                r.getList(r.fieldIndex(cleanerOutput())),
                mapMessage(r)));

//        return new DocumentCollection<>(features, CLASS, new VocabularyTermsModel(vectorizerModel.vocabulary()));
        return new DocumentCollection<>(features, CLASS, t -> hashingTF.indexOf(t));
    }

    private Msg mapMessage(org.apache.spark.sql.Row r) {
        return Msg.builder()
                .channelId(r.getAs("chat_id"))
                .id(r.getAs("message_id"))
                .internalId(r.getAs("internal_message_id"))
                .textContent(r.getAs("text_content"))
                .publishDate(r.getAs("publish_date"))
                .loadDate(r.getAs("load_date"))
                .type(r.getAs("type"))
                .viewCount(r.getAs("view_count")).build();
    }

    @Nonnull
    @Override
    protected Class<Document<Msg>> getEntityClass() {
        return (Class<Document<Msg>>)(new Document<Msg>().getClass());
    }

}
