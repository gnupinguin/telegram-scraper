package io.github.gnupinguin.analyzer.estimator

import org.apache.spark.ml.Model
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.functions.{col, collect_list, udf}
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

class TopicCoherenceModel(override val uid: String,
                          val texts: Dataset[_]) extends Model[TopicCoherenceModel] {

  def this(texts: Dataset[_]) = this(Identifiable.randomUID("topic-coherence-model"), texts)

  override def copy(extra: ParamMap): TopicCoherenceModel = defaultCopy(extra)

  override def transform(topicTerms: Dataset[_]): DataFrame = {
    def transformUDF = udf(topicCoherence)

    val docTerms = texts.select(col("normalized")).agg(collect_list("normalized").as("docTerms"))
    topicTerms.crossJoin(docTerms)
      .withColumn("topicCoherence", transformUDF(col("docTerms"), col("topicTerms")))
      .drop("docTerms")
  }

  override def transformSchema(schema: StructType): StructType = schema.add(name = "topicCoherence", dataType = DataTypes.DoubleType, nullable = false)

  private def topicCoherence: (Array[Array[String]], Array[String]) => Double = { (docTerms, topicTerms) =>
      uniquePairs(topicTerms)
      .map(p => {
        val stats = docTerms.map(terms => {
          val firstTermPresent = terms.contains(p._1)
          val bothTermPresent = firstTermPresent && terms.contains(p._2)
          (boolToInt(firstTermPresent), boolToInt(bothTermPresent)) //pair of (D(W_i), D(W_i, W_j))
        }).fold((0, 0)) { (p1, p2) => (p1._1 + p2._1, p1._2 + p2._2) }
        Math.log((stats._2 + 1.0) / stats._1) / Math.log(2)
      }).sum
  }

  private def uniquePairs(array: Array[String]) = {
    assert(array.length >= 2)
    array.slice(0, array.length - 2)
      .zipWithIndex
      .flatMap(p => array.slice(p._2 + 1, array.length - 1)
        .map(term => (p._1, term))) // (W_i, W_j), forall i < j
  }

  private def boolToInt(b: Boolean) = if (b) 1 else 0

}
