package io.github.gnupinguin.analyzer;

import io.github.gnupinguin.analyzer.datasource.SparkDataSource;
import io.github.gnupinguin.analyzer.nlp.TfIdfEstimator;
import io.github.gnupinguin.analyzer.nlp.TfIdfModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.apache.spark.sql.functions.*;

@Slf4j
public abstract class BaseLdaTest implements Serializable {

    protected static final String VOCABULARY_PATH = "NLP_RESULT/vocabulary";
    protected static final String TFIDF_PATH = "NLP_RESULT/tf-idf";

    protected abstract SparkSession getSpark();
    protected abstract SparkDataSource getSparkDataSource();
    protected abstract TfIdfEstimator getTfIdfEstimator();

    protected String[] vocabulary;

    protected void readVocabulary() {
        scala.collection.mutable.WrappedArray<String> vocabulary = getSpark().read().load(VOCABULARY_PATH).first().getAs(0);
        this.vocabulary = (String[])vocabulary.array();
    }

    protected Dataset<Row> loadTfIdf() {
        return loadTfIdf(false);
    }

    protected Dataset<Row> loadTfIdf(boolean force) {
        try {
            if (!force) {
                readVocabulary();
                return getSpark().read().load(TFIDF_PATH);
            }
        } catch (Exception e) {
            log.info("Data not found, tf-idf pipeline will be started", e);
        }
        var data = getSparkDataSource().load();
        TfIdfModel tfIdfModel = getTfIdfEstimator().fit(data);
        tfIdfModel.getCountVectorizerModel().vocabulary();
        var transformed = tfIdfModel.transform(data);
        transformed.write().mode(SaveMode.Overwrite).save(TFIDF_PATH);

        vocabulary = tfIdfModel.getCountVectorizerModel().vocabulary();
        var vocabularyDf = getSpark().createDataFrame(List.of(RowFactory.create(new String[][]{vocabulary})), new StructType(new StructField[]{
                new StructField("vocabulary", DataTypes.createArrayType(DataTypes.StringType), false, Metadata.empty())
        }));
        vocabularyDf.write().mode(SaveMode.Overwrite).save(VOCABULARY_PATH);

        return transformed;
    }

    protected void showStats(Dataset<Row> data) {
        UserDefinedFunction documentLength = documentLengthUdf();

        var res = data.select(count(col("*")).as("docs count"));

        var avgDocsSize = data.select(documentLength.apply(col("tf")).as("length"))
                .agg(avg("length").as("avg(docLength)"));
        var medianDocsSize = data.select(documentLength.apply(col("tf")).as("length"))
                .stat().approxQuantile("length", new double[]{0.5}, 0.25)[0];
        res.crossJoin(avgDocsSize)
                .withColumn("median length", lit(medianDocsSize))
                .show(true);
    }

    private UserDefinedFunction documentLengthUdf() {
        UserDefinedFunction topicTerms = udf((SparseVector docTerms) ->
                        Arrays.stream(docTerms.values()).sum(), DataTypes.DoubleType);
        getSpark().udf().register("docLength", topicTerms);
        return topicTerms;
    }

    protected Dataset<Row> showDocLengthDistribution(Dataset<Row> data) {
        UserDefinedFunction documentLength = documentLengthUdf();
        return data.select(documentLength.apply(col("tf")).as("length"))
                .select(collect_list("length"));
    }

    protected LDA getLda(int k) {
        LDA lda = new LDA();
        lda.setOptimizer("online")
                .setK(k)
                .setMaxIter(60)
                .setSeed(123)
                .setFeaturesCol("tfidf")
                .setTopicDistributionCol("topicDistribution");
        return lda;
    }

}
