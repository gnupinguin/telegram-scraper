package io.github.gnupinguin.analyzer;

import com.johnsnowlabs.nlp.DocumentAssembler;
import com.johnsnowlabs.nlp.Finisher;
import com.johnsnowlabs.nlp.annotators.LemmatizerModel;
import com.johnsnowlabs.nlp.annotators.Normalizer;
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector;
import io.github.gnupinguin.analyzer.nlp.NormalizedDocumentLengthFilter;
import io.github.gnupinguin.analyzer.nlp.TfIdfEstimator;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public SparkSession localSparkSession() {
        return SparkSession.builder()
                .appName("Local application")
                .master("local[*]")
                .getOrCreate();
    }

    @Bean
    public JavaSparkContext javaSparkContext(SparkSession spark) {
        return JavaSparkContext.fromSparkContext(spark.sparkContext());
    }

    @Bean
    public TfIdfEstimator tfIdfEstimator() {
        var documentAssembler = new DocumentAssembler();
        documentAssembler.setInputCol("text_content");
        documentAssembler.setOutputCol("document");

        var sentenceDetector = new SentenceDetector();
        sentenceDetector.setInputCols(new String[]{"document"});
        sentenceDetector.setOutputCol("sentence");

        var regexTokenizer = new com.johnsnowlabs.nlp.annotators.Tokenizer();
        regexTokenizer.setInputCols(new String[]{"sentence"});
        regexTokenizer.setOutputCol("token");
        regexTokenizer.addSplitChars("-");

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

        var stopWords = new ArrayList<>(Arrays.asList(StopWordsRemover.loadDefaultStopWords("russian")));
        stopWords.addAll(List.of("это", "оно", "который", "век", "секунда"));

        StopWordsRemover stopWordsRemover = new StopWordsRemover();
        stopWordsRemover.setInputCol("finished_normalized");
        stopWordsRemover.setStopWords(stopWords.toArray(new String[0]));
        stopWordsRemover.setOutputCol("tokens");

        NormalizedDocumentLengthFilter lengthFilter = new NormalizedDocumentLengthFilter(5, "tokens", "tokens");

        CountVectorizer countVectorizer = new CountVectorizer()
                .setInputCol("tokens")
//                .setMinDF(10)
                .setOutputCol("tf");

        IDF idf = new IDF()
                .setInputCol("tf")
                .setOutputCol("tfidf");

        return new TfIdfEstimator(documentAssembler, sentenceDetector, regexTokenizer, lemmatizer, normalizer, finisher, stopWordsRemover, lengthFilter, countVectorizer, idf);
    }

}
