package io.github.gnupinguin.analyzer.tokenizer;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import javax.annotation.Nonnull;
import java.io.Serializable;

public interface Tokenizer<T extends Serializable> extends Serializable {

    Dataset<TokenizedEntity<T>> map(@Nonnull Dataset<Row> data);

}
