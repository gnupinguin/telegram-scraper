package io.github.gnupinguin.analyzer.estimator

import org.apache.spark.ml.Model
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

class TopicCoherenceModel(override val uid: String,
                          val texts: Dataset[_]) extends Model[TopicCoherenceModel] {

  def this(texts: Dataset[_]) = this(Identifiable.randomUID("topic-coherence-model"), texts)

  override def copy(extra: ParamMap): TopicCoherenceModel = defaultCopy(extra)

  override def transform(topicTerms: Dataset[_]): DataFrame = {
    val aggTopicTerms = heightTriangleTermsMatrix(topicTerms)
    val topicPairOccurrencePerDoc = occurrencePairsTable(aggTopicTerms)
    val coherences = topicCoherences(topicPairOccurrencePerDoc)

    topicTerms.join(coherences, coherences("topicId") === topicTerms("topic"), "inner")
      .drop("topicId")
  }

  private def topicCoherences(topicPairOccurrencePerDoc: DataFrame): DataFrame = {
    def topicCoherenceTransform = udf(topicCoherence)
    topicPairOccurrencePerDoc.withColumn("topicCoherence", topicCoherenceTransform(col("topicStats")))
      .drop("topicStats")
      .withColumn("topicId", monotonically_increasing_id())
  }

  private def heightTriangleTermsMatrix(topicTerms: Dataset[_]): DataFrame = {
    def pairsTransform = udf(heightTriangle)
    topicTerms.select(pairsTransform(col("termIndices")).as("topicPairs"))
      .agg(collect_list("topicPairs").as("topicPairs"))
  }

  private def occurrencePairsTable(aggTopicTerms: DataFrame): DataFrame = {
    def pairTermsOccurrencesPerDocumentTransform = udf(pairTermsOccurrencesPerDocument)
    def aggregateTopicsTransform = udaf(TopicPairsAggregator, TopicPairsAggregator.encoderIn)
    texts.crossJoin(aggTopicTerms).withColumn("topicStats", pairTermsOccurrencesPerDocumentTransform(col("tf"), col("topicPairs")))
      .select("topicStats").agg(aggregateTopicsTransform(col("topicStats")).as("topicStats"))
      .select(explode(col("topicStats")).as("topicStats"))
  }

  override def transformSchema(schema: StructType): StructType = schema.add(name = "topicCoherence", dataType = DataTypes.DoubleType, nullable = false)

  private def heightTriangle: Array[Int] => Array[(Int, Int)] = { topicTermsIndices =>
    assert(topicTermsIndices.length >= 2)
    topicTermsIndices.slice(0, topicTermsIndices.length - 2).zipWithIndex.flatMap { p =>
      topicTermsIndices.slice(p._2 + 1, topicTermsIndices.length - 1)
        .map(term => (p._1, term))
    } // (W_i, W_j), forall i < j
  }

  private def boolToInt(b: Boolean) = if (b) 1 else 0

  def pairTermsOccurrencesPerDocument: (SparseVector, Array[Array[(Int, Int)]]) => Array[Array[(Int, Int)]] = { (docTerms, topics) =>
    topics.map{topicPairs =>
      topicPairs.map {p =>
        val firstTermPresent = docTerms(p._1) > 0
        val bothTermPresent = firstTermPresent && (docTerms(p._2) > 0)
        (boolToInt(firstTermPresent), boolToInt(bothTermPresent))
      }
    }
  }

  private def topicCoherence: Array[(Int, Int)] => Double = { topicStats =>
    topicStats.map {
      case (x, y) => Math.log((y + 1.0) / x) / Math.log(2)
    }.sum
  }

}
