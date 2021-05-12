package io.github.gnupinguin.analyzer;

import io.github.gnupinguin.analyzer.configuration.Profiles;
import io.github.gnupinguin.analyzer.datasource.SparkDataSource;
import io.github.gnupinguin.analyzer.nlp.TfIdfEstimator;
import io.github.gnupinguin.analyzer.transformer.MaxItemIndexTransformer;
import io.github.gnupinguin.analyzer.transformer.TopIndexesTransformer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

@Slf4j
@Getter(AccessLevel.PROTECTED)
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({Profiles.REAL_DATA})
public class TopicsDistributionTest extends BaseLdaTest {

    public static final String LDA_TRANSFORM = "NLP_RESULT/lda-model";
    public static final String LDA_TOPICS = "NLP_RESULT/lda-topic";

    @Autowired
    private SparkSession spark;

    @Autowired
    private TfIdfEstimator tfIdfEstimator;

    @Autowired
    private SparkDataSource sparkDataSource;

    private Dataset<Row> topics;

    @Test
    public void showTopics() {
        var data = loadLda(false);

        MaxItemIndexTransformer maxItemIndexTransformer = new MaxItemIndexTransformer();
        maxItemIndexTransformer.setInputCol("topicDistribution");
        maxItemIndexTransformer.setOutputCol("mainTopic");
        Dataset<Row> indexed = maxItemIndexTransformer.transform(data).select("channel", "internal_id", "tokens", "mainTopic");

        //topics description
        termsIndicesToTerms(topics)
                .select("topic", "terms")
                .orderBy("topic")
                .show(400, false);

        //topics distribution (histogram) by posts
        indexed.select("mainTopic")
                .groupBy("mainTopic")
                .agg(count("mainTopic").as("topicCount"))
                .orderBy(desc("topicCount"))
                .show();

    }

    @Test
    public void testMultipleTopicDistribution() {
        var data = loadLda(false);

        TopIndexesTransformer topIndexesTransformer = new TopIndexesTransformer(3, 0.3);
        topIndexesTransformer.setInputCol("topicDistribution");
        topIndexesTransformer.setOutputCol("mainTopics");
        Dataset<Row> indexed = topIndexesTransformer.transform(data).select("channel", "internal_id", "tokens", "mainTopics");

        //topics description
        termsIndicesToTerms(topics)
                .select("topic", "terms")
                .orderBy("topic")
                .show(400, false);

        //topics distribution (histogram) by posts
        indexed.select(explode(col("mainTopics")).as("mainTopic"))
                .groupBy("mainTopic")
                .agg(count("mainTopic").as("topicCount"))
                .orderBy(desc("topicCount"))
                .show();
    }

    private Dataset<Row> termsIndicesToTerms(Dataset<Row> data) {
        UserDefinedFunction topicTerms = udf(
                (scala.collection.mutable.WrappedArray<Integer> termIndices) ->
                        scala.collection.JavaConverters.seqAsJavaList(termIndices).stream()
                                .map(i -> vocabulary[i])
                                .collect(Collectors.toList()), DataTypes.createArrayType(DataTypes.StringType)
        );
        return data.withColumn("terms", topicTerms.apply(col("termIndices")))
                .drop("termIndices");
    }

    private Dataset<Row> loadLda(boolean force) {
        try {
            if (!force) {
                readVocabulary();
                topics = spark.read().load(LDA_TOPICS);
                return spark.read().load(LDA_TRANSFORM);
            }
        } catch (Exception e) {
            log.info("LDA data not found ", e);
        }
        LDA lda = getLda(400);
        Dataset<Row> data = loadTfIdf();
        LDAModel ldaModel = lda.fit(data);
        topics = ldaModel.describeTopics();
        topics.write().mode(SaveMode.Overwrite).save(LDA_TOPICS);

        Dataset<Row> transform = ldaModel.transform(data);
        transform.write().mode(SaveMode.Overwrite).save(LDA_TRANSFORM);

        return transform;
    }

}
