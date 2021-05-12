package io.github.gnupinguin.analyzer;

import io.github.gnupinguin.analyzer.configuration.Profiles;
import io.github.gnupinguin.analyzer.datasource.SparkDataSource;
import io.github.gnupinguin.analyzer.nlp.TfIdfEstimator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.apache.spark.sql.functions.*;


@Slf4j
@Getter(AccessLevel.PROTECTED)
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({Profiles.REAL_DATA})
public class TopWordsTest extends BaseLdaTest{

    @Autowired
    private SparkSession spark;

    @Autowired
    private TfIdfEstimator tfIdfEstimator;

    @Autowired
    private SparkDataSource sparkDataSource;

    @Test
    public void topWords() {
        var data = loadTfIdf();
        var topWords = data.select(explode(col("tokens")).as("term"))
                .groupBy(col("term"))
                .agg(count("term").as("count"))
                .orderBy(desc("count"));

        var terms = topWords.select(collect_list("term").as("terms"));
        var counts = topWords.select(collect_list("count").as("counts"));
        terms.crossJoin(counts)
                .write().mode(SaveMode.Overwrite).json("/home/gnupinguin/Projects/NLP_RESULT/top-words");
    }

    @Test
    public void wordContains() {
        var data = loadTfIdf();
        data.where(array_contains(col("tokens"), "год")).select("text_content","tokens")
                .show(2,false);
    }
}
