package io.github.gnupinguin.analyzer.datasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.Serializable;

public interface SparkDataSource extends Serializable {

    Dataset<Row> load();

}
