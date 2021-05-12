package io.github.gnupinguin.analyzer;

import io.github.gnupinguin.analyzer.configuration.Profiles;
import io.github.gnupinguin.analyzer.datasource.SparkDataSource;
import io.github.gnupinguin.analyzer.estimator.TopicCoherence;
import io.github.gnupinguin.analyzer.nlp.TfIdfEstimator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.spark.sql.functions.avg;
import static org.apache.spark.sql.functions.col;

@Slf4j
@Getter(AccessLevel.PROTECTED)
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({Profiles.TEST_DATA})
public class CoherenceTest extends BaseLdaTest {

    @Autowired
    private SparkSession spark;

    @Autowired
    private TfIdfEstimator tfIdfEstimator;

    @Autowired
    private SparkDataSource sparkDataSource;


    /**
     +----------+-----------------+-------------+
     |docs count|   avg(docLength)|median length|
     +----------+-----------------+-------------+
     |     31606|33.54575080680883|          5.0|
     +----------+-----------------+-------------+
     */

    @Test
    public void testCoherence() {
        List<Double> coherences = new ArrayList<>(List.of(-150.0645986778987, -154.77889576339285, -158.4448418324371, -157.92069257286738, -150.24683902841554, -144.8559634980129, -133.07365251247433, -128.00533033182506, -124.79310909160856, -126.4764098859392, -119.99019301947747, -116.70341334574083, -109.33526486223376, -108.50932014867807, -102.72524966176915, -98.58102601604061, -96.08331862658402, -90.25255845229813, -86.45881750855365, -84.94817409034775, -81.1075980542551, -81.97178616693752, -81.90319704638691, -78.47311945383647, -79.21230677402285, -75.3648735549614, -70.71954485847118, -71.40671450572613, -68.3564828593982, -67.45233841291069, -68.50007100113345, -68.98358535948452, -67.66202204629047, -67.63962865167628, -64.98921655084702, -65.07164048564879, -64.11887199652581, -62.95786170166941));
        Dataset<Row> tfIdf = loadTfIdf(true);
        tfIdf.persist();
        List<Integer> topicCount = List.of(10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
        logFile("Start1");
        logFile("Start2");
        topicCount.forEach(k -> {
            LDA lda = getLda(k);
            LDAModel ldaModel = lda.fit(tfIdf);
            var topics = ldaModel.describeTopics(10).select("topic", "termIndices");
            topics.persist();

            TopicCoherence topicCoherence = new TopicCoherence();
            var coherenceModel = topicCoherence.fit(tfIdf.select(col("tf")));
            var coherence = coherenceModel.transform(topics);

            var totalCoherence = coherence.agg(avg("topicCoherence").as("totalCoherence")).first().getDouble(0);
            coherences.add(totalCoherence);
            logFile("Coherences("+ coherences.size() + "): " + coherences);
        });
    }

    private void logFile(String message) {
        try {
            message = new Date() + "\n" + message;
            System.out.println(message);
            Files.writeString(Path.of("logs.txt"), message);
        } catch (Exception e) {
            System.out.println("Could not log exception");
        }
    }

}
