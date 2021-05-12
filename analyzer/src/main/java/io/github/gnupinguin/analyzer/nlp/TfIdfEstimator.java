package io.github.gnupinguin.analyzer.nlp;

import com.johnsnowlabs.nlp.DocumentAssembler;
import com.johnsnowlabs.nlp.Finisher;
import com.johnsnowlabs.nlp.annotators.LemmatizerModel;
import com.johnsnowlabs.nlp.annotators.Normalizer;
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector;
import org.apache.spark.ml.Estimator;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.util.Identifiable;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.types.StructType;

import java.util.Arrays;

public class TfIdfEstimator extends Estimator<TfIdfModel> {

    private final String UID = Identifiable.randomUID("tf_idf-estimator");

    private final Pipeline pipeline;

    public TfIdfEstimator(DocumentAssembler documentAssembler,
                          SentenceDetector sentenceDetector,
                          com.johnsnowlabs.nlp.annotators.Tokenizer tokenizer,
                          LemmatizerModel lemmatizerModel,
                          Normalizer normalizer,
                          Finisher finisher,
                          StopWordsRemover stopWordsRemover,
                          NormalizedDocumentLengthFilter lengthFilter,
                          CountVectorizer countVectorizer,
                          IDF idf) {
        pipeline = new Pipeline()
                .setStages(new PipelineStage[]{
                        documentAssembler,
                        sentenceDetector,
                        tokenizer,
                        lemmatizerModel,
                        normalizer,
                        finisher,
                        stopWordsRemover,
                        lengthFilter,
                        countVectorizer,
                        idf
                });
    }

    @Override
    public TfIdfModel fit(Dataset<?> dataset) {
        PipelineModel pipelineModel = pipeline.fit(dataset);
        return new TfIdfModel(pipelineModel, getModel(CountVectorizerModel.class, pipelineModel));
    }

    @Override
    public Estimator<TfIdfModel> copy(ParamMap extra) {
        return defaultCopy(extra);
    }

    @Override
    public StructType transformSchema(StructType schema) {
        return pipeline.transformSchema(schema);
    }

    @Override
    public String uid() {
        return UID;
    }

    private <T> T getModel(Class<T> type, PipelineModel models) {
        return Arrays.stream(models.stages())
                .filter(type::isInstance)
                .findFirst()
                .map(type::cast)
                .orElseThrow(RuntimeException::new);
    }

}
