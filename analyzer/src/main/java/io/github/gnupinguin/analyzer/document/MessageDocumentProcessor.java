package io.github.gnupinguin.analyzer.document;

import io.github.gnupinguin.analyzer.entity.Msg;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.Serializable;

public interface MessageDocumentProcessor extends Serializable {
    DocumentCollection<Msg, Document<Msg>> map(Dataset<Row> data);
}
