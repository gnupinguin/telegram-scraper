package io.github.gnupinguin.analyzer.tokenizer;

import io.github.gnupinguin.analyzer.entity.Query;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class QueryTokenizerImpl extends AbstractPipeline<TokenizedEntity<Query>> implements QueryTokenizer {

    private final SparkSession spark;

    public QueryTokenizerImpl(SparkSession spark) {
        super("text");
        this.spark = spark;
    }

    @Nonnull
    @Override
    protected Class<TokenizedEntity<Query>> getEntityClass() {
        return (Class<TokenizedEntity<Query>>)(new TokenizedEntity<>(List.of(), new Query(null)).getClass());
    }

    @Override
    public Dataset<TokenizedEntity<Query>> map(@Nonnull Dataset<Row> data) {
        return transformRows(data, r -> new TokenizedEntity<>( r.getList(r.fieldIndex(cleanerOutput())),
                new Query(r.getAs("text"))));
    }

    @Nonnull
    @Override
    public TokenizedEntity<Query> map(@Nonnull Query query) {
        var dataFrame = spark.createDataFrame(List.of(query), Query.class);
        return map(dataFrame).first();
    }

}
