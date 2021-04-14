package io.github.gnupinguin.analyzer.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.spark.ml.linalg.SparseVector;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document<T extends Serializable> implements Serializable {

    private SparseVector rank;

    private List<String> tokens;

    private T content;

}
