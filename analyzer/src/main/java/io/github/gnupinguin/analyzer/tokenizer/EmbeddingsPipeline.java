package io.github.gnupinguin.analyzer.tokenizer;

import com.johnsnowlabs.nlp.DocumentAssembler;
import com.johnsnowlabs.nlp.Finisher;
import com.johnsnowlabs.nlp.annotators.LemmatizerModel;
import com.johnsnowlabs.nlp.annotators.Normalizer;
import com.johnsnowlabs.nlp.annotators.ner.NerConverter;
import com.johnsnowlabs.nlp.annotators.ner.dl.NerDLModel;
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector;
import com.johnsnowlabs.nlp.embeddings.WordEmbeddings;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.DistributedLDAModel;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import scala.Tuple2;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//@Service
public class EmbeddingsPipeline implements Serializable {

    public Dataset<Row> apply(Dataset<Row> data) {
        DocumentAssembler documentAssembler = (DocumentAssembler)((DocumentAssembler) new DocumentAssembler()
                .setInputCol("text_content"))
                .setOutputCol("document");

        SentenceDetector sentenceDetector = (SentenceDetector) ((SentenceDetector) new SentenceDetector()
                .setInputCols(new String[]{"document"}))
                .setOutputCol("sentence");

        com.johnsnowlabs.nlp.annotators.Tokenizer regexTokenizer = (com.johnsnowlabs.nlp.annotators.Tokenizer)((com.johnsnowlabs.nlp.annotators.Tokenizer) new com.johnsnowlabs.nlp.annotators.Tokenizer()
                .setInputCols(new String[]{"sentence"}))
                .setOutputCol("token");

        LemmatizerModel lemmatizer = (LemmatizerModel)LemmatizerModel.load("/home/gnupinguin/Projects/NLP_DATA/lemma");
        lemmatizer.setInputCols(new String[]{"token"});
        lemmatizer.setOutputCol("lemma");

        Normalizer normalizer = (Normalizer)((Normalizer) new Normalizer()
                .setLowercase(true)
                .setInputCols(new String[]{"lemma"}))
                .setOutputCol("normalized");

        WordEmbeddings wordEmbeddings = new WordEmbeddings();
        wordEmbeddings.setInputCols(new String[]{"document", "normalized"});
        wordEmbeddings.setOutputCol("embeddings");
        wordEmbeddings.setDimension(100);
        wordEmbeddings.setStorageRef("glove_100d");
        wordEmbeddings.setStoragePath("/home/gnupinguin/Projects/NLP_DATA/empty.txt", "TEXT");

        NerDLModel ner = (NerDLModel)NerDLModel.load("/home/gnupinguin/Projects/NLP_DATA/embeddings/");
        ner.setInputCols(new String[]{"document", "normalized", "embeddings"});
        ner.setOutputCol("ner");

        NerConverter nerConverter = (NerConverter)(((NerConverter)new NerConverter()
                .setInputCols(new String[]{"document", "normalized", "ner"}))
                .setOutputCol("ner_converter"));

        Finisher finisher = new Finisher()
                .setInputCols(new String[]{"ner_converter"})
//                .setOutputCols(new String[]{"norm"})
//                .setInputCols("")
//                .setOutputAsArray(true);
                .setCleanAnnotations(true)
        ;


        StopWordsRemover stopWordsRemover = (StopWordsRemover) ((StopWordsRemover) new StopWordsRemover()
                .setInputCol("finished_ner_converter")
                .setStopWords(StopWordsRemover.loadDefaultStopWords("russian")))
                .setOutputCol("tokens");

        CountVectorizer countVectorizer = new CountVectorizer()
                .setInputCol("tokens")
//                .setMinDF(2)
//                .setMaxDF(0.8)
                .setOutputCol("tf");

        IDF idf = new IDF()
                .setInputCol("tf")
                .setOutputCol("idf");

        Pipeline nlpPipeline = new Pipeline()
                .setStages(new PipelineStage[]{
                        documentAssembler,
                        sentenceDetector,
                        regexTokenizer,
                        lemmatizer,
                        normalizer,
                        wordEmbeddings,
                        ner,
                        nerConverter,
                        finisher,
                        stopWordsRemover,
                        countVectorizer,
                        idf
                        });

        // add (1.0 / actualCorpusSize) to MiniBatchFraction be more robust on tiny datasets.
//        case "online" => new OnlineLDAOptimizer().setMiniBatchFraction(0.05 + 1.0 / actualCorpusSize)
        PipelineModel pipelineModel = nlpPipeline.fit(data);
        data = pipelineModel.transform(data);
        LDA lda = new LDA();
        lda
                .setOptimizer("online")
                .setK(300)
                .setMaxIter(20)
//                .setDocConcentration(0.1)
//                .setTopicConcentration(0.03)
                .setFeaturesCol("idf")
                .setTopicDistributionCol("topic_dist");

        var startTime = System.currentTimeMillis();
        LDAModel ldaModel = lda.fit(data);
        var endTime = System.currentTimeMillis();


        System.out.println("Finished training LDA model.  Summary:");
        System.out.println("Spent time: " + (endTime - startTime) / 1000);

        if (ldaModel instanceof DistributedLDAModel) {
            var distLDAModel = (DistributedLDAModel)ldaModel;
            var avgLogLikelihood = distLDAModel.trainingLogLikelihood() / data.count();
            System.out.println("Training data average log likelihood: " + avgLogLikelihood);
            System.out.println();
        }

        var topicIndices = ldaModel.describeTopics(10);

        String[] vocabulary = Arrays.stream(pipelineModel.stages())
                .filter(stage -> stage instanceof CountVectorizerModel)
                .map(stage -> ((CountVectorizerModel) stage).vocabulary())
                .findFirst().get();
        topicIndices
                .map((MapFunction<Row, List<Tuple2<Integer, Double>>>)  r -> zip(r.getList(1), r.getList(2)), e1())
                .foreach((ForeachFunction<List<Tuple2<Integer, Double>>>)  ps -> System.out.println(getCollect(vocabulary, ps)));


        data = ldaModel.transform(data);

//        Row[] take = (Row[])data.take(10);
//        Arrays.stream(take)
//                .map(r -> load(r, vocabulary))
//                .forEach(System.out::println);

        return ldaModel.describeTopics();

    }

    private String load(Row r, String[] vocabulary) {
        String text = r.getAs("text_content");
        DenseVector topic = r.getAs("topic_dist");

        return Arrays.stream(topic.toSparse().indices())
                .mapToObj(i -> new Tuple2<>(vocabulary[i], topic.apply(i)))
                .map(p -> String.format("(%s, %f)", p._1(), p._2()))
                .limit(10)
                .collect(Collectors.joining("")) + "\n" + text + "\n";
    }

    @Nonnull
    private String getCollect(String[] vocabulary, List<Tuple2<Integer, Double>> ps) {
        return ps.stream()
                .map(p -> new Tuple2<>(vocabulary[p._1()], p._2))
                .map(p -> String.format("(%s, %f)", p._1(), p._2()))
                .collect(Collectors.joining(", ")) + "\n";
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
