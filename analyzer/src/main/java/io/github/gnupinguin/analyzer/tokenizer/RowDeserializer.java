package io.github.gnupinguin.analyzer.tokenizer;

import org.apache.spark.sql.Row;

import javax.annotation.Nonnull;
import java.io.Serializable;

public interface RowDeserializer<T extends Serializable> extends Serializable {
    T map(@Nonnull Row row);
}
