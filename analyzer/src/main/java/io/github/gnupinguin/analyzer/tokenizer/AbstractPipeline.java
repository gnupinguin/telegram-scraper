package io.github.gnupinguin.analyzer.tokenizer;

import com.johnsnowlabs.nlp.DocumentAssembler;
import com.johnsnowlabs.nlp.Finisher;
import com.johnsnowlabs.nlp.annotators.LemmatizerModel;
import com.johnsnowlabs.nlp.annotators.Normalizer;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;

import javax.annotation.Nonnull;
import java.io.Serializable;

abstract public class AbstractPipeline<T extends Serializable> implements Serializable{

    private final String pipelineInputColumn;
    protected final Pipeline basePipeline;

    public AbstractPipeline(String pipelineInputColumn) {
        this.pipelineInputColumn = pipelineInputColumn;
        DocumentAssembler assembler = (DocumentAssembler)((DocumentAssembler) new DocumentAssembler()
                .setInputCol(pipelineInputColumn))
                .setOutputCol(assemblerOutput());

        com.johnsnowlabs.nlp.annotators.Tokenizer tokenizer = (com.johnsnowlabs.nlp.annotators.Tokenizer)((com.johnsnowlabs.nlp.annotators.Tokenizer) new com.johnsnowlabs.nlp.annotators.Tokenizer()
                .setInputCols(new String[]{assemblerOutput()}))
                .setOutputCol(tokenizerOutput());

        LemmatizerModel lemmatizer = (LemmatizerModel) ((LemmatizerModel) LemmatizerModel
                .pretrained("lemma", "ru")
                .setInputCols(new String[]{tokenizerOutput()}))
                .setOutputCol(lemmatizerOutput());

        Normalizer normalizer = (Normalizer)((Normalizer) new Normalizer()
                .setLowercase(true)
                .setInputCols(new String[]{lemmatizerOutput()}))
                .setOutputCol(normalizerOutput());

        Finisher finisher = new Finisher()
                .setInputCols(new String[]{normalizerOutput()})
                .setOutputCols(new String[]{normalizerOutput()})
                .setOutputAsArray(true);
//        StopWordsCleaner cleaner = (StopWordsCleaner)((StopWordsCleaner) new StopWordsCleaner()
//                .setStopWords(StopWordsRemover.loadDefaultStopWords("russian"))
//                .setInputCols(new String[]{normalizerOutput()}))
//                .setOutputCol(cleanerOutput());
        StopWordsRemover stopWordsRemover = (StopWordsRemover) ((StopWordsRemover) new StopWordsRemover()
                .setInputCol(normalizerOutput())
                .setStopWords(StopWordsRemover.loadDefaultStopWords("russian")))
                .setOutputCol(cleanerOutput());

        basePipeline = new Pipeline()
                .setStages(new PipelineStage[]{assembler, tokenizer, lemmatizer, normalizer, finisher, stopWordsRemover});
    }

    protected String pipelineInput() {
        return pipelineInputColumn;
    }

    protected String assemblerOutput() {
        return "doc";
    }

    protected String tokenizerOutput() {
        return "tokens";
    }

    protected String lemmatizerOutput() {
        return "tokens";
    }

    protected String normalizerOutput() {
        return "normalized";
    }

    protected String cleanerOutput() {
        return "filtered";
    }

    @Nonnull
    abstract protected Class<T> getEntityClass();

    @Nonnull
    protected Encoder<T> getTokenizedEntityEncoder() {
        return Encoders.kryo(getEntityClass());
    }

    public Dataset<T> transformRows(@Nonnull Dataset<Row> data, MapFunction<Row, T> mapper) {
        return basePipeline.fit(data)
                .transform(data)
                .map(mapper, getTokenizedEntityEncoder());
    }

    public Dataset<T> mapRows(@Nonnull Dataset<Row> data,
                                 @Nonnull MapFunction<Row, T> mapper) {
        return data.map(mapper, getTokenizedEntityEncoder());
    }

}
