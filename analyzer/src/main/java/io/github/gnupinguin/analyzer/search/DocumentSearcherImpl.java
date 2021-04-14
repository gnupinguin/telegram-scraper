package io.github.gnupinguin.analyzer.search;

import io.github.gnupinguin.analyzer.document.Document;
import io.github.gnupinguin.analyzer.document.DocumentCollection;
import io.github.gnupinguin.analyzer.entity.Query;
import io.github.gnupinguin.analyzer.tokenizer.QueryTokenizer;
import lombok.AllArgsConstructor;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.apache.spark.sql.functions.col;

@Service
@AllArgsConstructor
public class DocumentSearcherImpl implements DocumentSearcher {

    private final QueryTokenizer queryTokenizer;
    private final SparkSession spark;

    @Override
    public <T extends Serializable, R extends Document<T>> Dataset<Tuple2<Double, R>> search(DocumentCollection<T, R> collection,
                                                                             String query, int count) {
        final Set<String> terms = queryTerms((query));
        return collection.getDocuments()
                .map((MapFunction<R, Tuple2<Double, R>>) d -> scoredDocument(terms, d, collection), tupleEncoder(collection.getDocType()))
                .filter((FilterFunction<Tuple2<Double,R>>)  p -> p._1() > 0)
                .orderBy(col("_1").desc())
                .limit(count);
    }

    @Nonnull
    private <T extends Serializable, R extends Document<T>> Tuple2<Double, R> scoredDocument(Set<String> terms, R d, DocumentCollection<T, R> docs) {
        return new Tuple2<>(score(terms, d, docs), d);
    }

    private <T extends Serializable, R extends Document<T>> Encoder<Tuple2<Double, R>> tupleEncoder(Class<R> rClass) {
        return Encoders.tuple(Encoders.DOUBLE(), Encoders.kryo(rClass));
    }

    private Set<String> queryTerms(@Nonnull String query) {
        var queryTokenizedEntity = queryTokenizer.map(new Query(query));
        List<String> tokens = queryTokenizedEntity.getTokens();
        return new HashSet<>(tokens);
    }

    private double score(Set<String> queryTerms, Document<?> document, DocumentCollection<?, ?> collection) {
        List<String> docTerms = document.getTokens();
        return IntStream.range(0, docTerms.size())
                .filter(i -> queryTerms.contains(docTerms.get(i)))
                .map(i -> getTermIdentifier(collection, docTerms, i))
                .mapToDouble(i -> document.getRank().apply(i))
//                .mapToDouble(i -> document.getRank().values()[i])
                .sum();
    }

    private int getTermIdentifier(DocumentCollection<?, ?> collection, List<String> docTerms, int i) {
        return collection.getTermsModel().termIdentifier(docTerms.get(i));
    }

}
