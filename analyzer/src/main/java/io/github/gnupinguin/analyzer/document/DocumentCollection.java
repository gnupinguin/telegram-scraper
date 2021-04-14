package io.github.gnupinguin.analyzer.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.spark.sql.Dataset;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class DocumentCollection<T extends Serializable, R extends Document<T>> implements Serializable {

    private final Dataset<R> documents;
    private final Class<R> docType;
    private final TermsModel termsModel;

}
