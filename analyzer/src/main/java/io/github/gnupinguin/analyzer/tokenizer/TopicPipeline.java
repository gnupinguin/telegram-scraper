package io.github.gnupinguin.analyzer.tokenizer;

import com.johnsnowlabs.nlp.DocumentAssembler;
import com.johnsnowlabs.nlp.Finisher;
import com.johnsnowlabs.nlp.annotators.LemmatizerModel;
import com.johnsnowlabs.nlp.annotators.Normalizer;
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.sql.*;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class TopicPipeline implements Serializable {

    private final SparkSession spark;

    public Dataset<Row> apply(Dataset<Row> data) {
        Dataset<Row> res = null;
        List<Double> coherences = new ArrayList<>();
        Dataset<Row> tfIdf = tfIdfPipe().fit(data).transform(data);

        for (int k = 400; k <= 1000; k += 100) {
            LDA lda = new LDA();
            lda
                    .setOptimizer("online")
                    .setK(k)
                    .setMaxIter(20)
                    .setSeed(123)
                    .setFeaturesCol("tfidf")
                    .setTopicDistributionCol("topicDistribution");
            LDAModel ldaModel = lda.fit(tfIdf);

            var topics = ldaModel.describeTopics(10).select("termIndices");
            topics.persist();
        }




//        for (int k = 400; k <= 1000; k += 100) {
//            PipelineModel pipelineModel = newPipe(k).fit(data);
//
//            Dataset<Row> trained = pipelineModel.transform(data);
//            LDAModel ldaModel = getModel(LDAModel.class, pipelineModel);
//            String[] vocabulary = getModel(CountVectorizerModel.class, pipelineModel).vocabulary();
//            UserDefinedFunction topicTerms = udf(
//                    (scala.collection.mutable.WrappedArray<Integer> termIndices) ->
//                            scala.collection.JavaConverters.seqAsJavaList(termIndices).stream()
//                                    .map(i -> vocabulary[i])
//                                    .collect(Collectors.toList()), DataTypes.createArrayType(DataTypes.StringType)
//            );
//
//            //
//            topicTerms.asNonNullable();
//            spark.udf().register("ind2terms" + k, topicTerms);
//
//            var topics = ldaModel.describeTopics(10)
//                    .withColumn("topicTerms", topicTerms.apply(col("termIndices")));
//            topics.persist();
//
//            TopicCoherence topicCoherence = new TopicCoherence();
//            var coherenceModel = topicCoherence.fit(trained.select(col("finished_normalized").as("normalized")));
//            res = coherenceModel.transform(topics);
//            res.persist();
//
//            var totalCoherence = res.agg(avg("topicCoherence").as("totalCoherence")).first().getDouble(0);
//            coherences.add(totalCoherence);
//            System.out.println("Coherences: " + coherences);
//        }
        return res;
    }

    private Pipeline tfIdfPipe() {
        var documentAssembler = new DocumentAssembler();
        documentAssembler.setInputCol("text_content");
        documentAssembler.setOutputCol("document");

        var sentenceDetector = new SentenceDetector();
        sentenceDetector.setInputCols(new String[]{"document"});
        sentenceDetector.setOutputCol("sentence");

        var regexTokenizer = new com.johnsnowlabs.nlp.annotators.Tokenizer();
        regexTokenizer.setInputCols(new String[]{"sentence"});
        regexTokenizer.setOutputCol("token");

        LemmatizerModel lemmatizer = (LemmatizerModel)LemmatizerModel.load("/home/gnupinguin/Projects/NLP_DATA/lemma");
        lemmatizer.setInputCols(new String[]{"token"});
        lemmatizer.setOutputCol("lemma");

        Normalizer normalizer = new Normalizer();
        normalizer.setLowercase(true);
        normalizer.setInputCols(new String[]{"lemma"});
        normalizer.setOutputCol("normalized");
        normalizer.setCleanupPatterns(new String[]{
                "[^а-яёА-ЯЁ]+",
                "[^а-яёА-ЯЁ.-]+",
                "[^а-яёА-ЯЁ]+$"
        });
        normalizer.setMinLength(3);

        Finisher finisher = new Finisher();
        finisher.setInputCols(new String[]{"normalized"});
        finisher.setOutputAsArray(true);
        finisher.setCleanAnnotations(true);


        StopWordsRemover stopWordsRemover = new StopWordsRemover();
        stopWordsRemover.setInputCol("finished_normalized");
        stopWordsRemover.setStopWords(StopWordsRemover.loadDefaultStopWords("russian"));
        stopWordsRemover.setOutputCol("tokens");

        CountVectorizer countVectorizer = new CountVectorizer()
                .setInputCol("tokens")
//                .setMinDF(10)
                .setOutputCol("tf");

        IDF idf = new IDF()
                .setInputCol("tf")
                .setOutputCol("tfidf");

        //Create the model
        return new Pipeline()
                .setStages(new PipelineStage[]{
                        documentAssembler,
                        sentenceDetector,
                        regexTokenizer,
                        lemmatizer,
                        normalizer,
                        finisher,
                        stopWordsRemover,
                        countVectorizer,
                        idf,
                });
    }

    private String toVocabularyString(Row row, String[] vocabulary) {
        List<Integer> indexes = row.getList(row.fieldIndex("termIndices"));
        Integer topic = row.getInt(row.fieldIndex("topic"));
        return topic + " (" +
                indexes.stream()
                .map(i -> vocabulary[i])
                .collect(Collectors.joining(", ")) + ")";
    }

    private <T> T getModel(Class<T> type, PipelineModel models) {
        return Arrays.stream(models.stages())
                .filter(stage -> type.isInstance(stage))
                .findFirst()
                .map(type::cast)
                .orElseThrow(RuntimeException::new);
    }

    private <T, R> MapFunction<T, R> mapper(Function<T, R> m) {
        return (MapFunction<T, R>)m ;
    }

    private <T, R>  Encoder<List<Tuple2<T, R>>> e1() {
        List<Tuple2<T, R>> v = new ArrayList<>();
        return Encoders.kryo((Class<List<Tuple2<T, R>>>) v.getClass());
    }

    private <T, R> List<Tuple2<T, R>> zip(List<T> indexes, List<R> weights) {
        return IntStream.range(0, indexes.size())
                .mapToObj(i -> new Tuple2<>(indexes.get(i), weights.get(i)))
                .collect(Collectors.toList());
    }


}
