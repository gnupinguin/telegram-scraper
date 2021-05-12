package io.github.gnupinguin.analyzer.nlp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.util.Identifiable;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.size;

@Getter
@RequiredArgsConstructor
public class NormalizedDocumentLengthFilter extends Transformer {

    private final String UID = Identifiable.randomUID("normalized-length-transformer");

    private final int minLength;
    private final String inputCol;
    private final String outputCol;

    @Override
    public StructType transformSchema(StructType schema) {
        return schema.add(new StructField(getOutputCol(), DataTypes.createArrayType(DataTypes.StringType), false, Metadata.empty()));
    }

    @Override
    public Dataset<Row> transform(Dataset<?> dataset) {
        return dataset.where(size(col(getInputCol())).geq(minLength)).select("*");
    }

    @Override
    public Transformer copy(ParamMap extra) {
        return defaultCopy(extra);
    }

    @Override
    public String uid() {
        return UID;
    }

}
