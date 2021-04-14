package io.github.gnupinguin.analyzer.search;

import io.github.gnupinguin.analyzer.document.Document;
import io.github.gnupinguin.analyzer.document.DocumentCollection;
import org.apache.spark.sql.Dataset;
import scala.Tuple2;

import java.io.Serializable;

public interface DocumentSearcher extends Serializable {

    <T extends Serializable, R extends Document<T>> Dataset<Tuple2<Double, R>> search(DocumentCollection<T, R> collection,
                                                                                       String query, int count);
}
