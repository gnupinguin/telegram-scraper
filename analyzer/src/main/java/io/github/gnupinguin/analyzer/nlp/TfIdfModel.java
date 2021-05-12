package io.github.gnupinguin.analyzer.nlp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.spark.ml.Model;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.util.Identifiable;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

@RequiredArgsConstructor
public class TfIdfModel extends Model<TfIdfModel> {

    private final String UID = Identifiable.randomUID("tf_idf-model");

    private final PipelineModel pipelineModel;

    @Getter
    private final CountVectorizerModel countVectorizerModel;

    @Override
    public TfIdfModel copy(ParamMap extra) {
        return defaultCopy(extra);
    }

    @Override
    public StructType transformSchema(StructType schema) {
        return pipelineModel.transformSchema(schema);
    }

    @Override
    public Dataset<Row> transform(Dataset<?> dataset) {
        return pipelineModel.transform(dataset);
    }

    @Override
    public String uid() {
        return UID;
    }

}
