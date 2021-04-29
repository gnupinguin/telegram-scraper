package io.github.gnupinguin.analyzer.estimator

import org.apache.spark.internal.Logging
import org.apache.spark.ml.Estimator
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.param.shared.{HasInputCols, HasOutputCol}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types.{DataTypes, StructType}

class TopicCoherence(override val uid: String) extends Estimator[TopicCoherenceModel] with HasInputCols
  with HasOutputCol with Logging {

  def this() = this(Identifiable.randomUID("topic-coherence-estimator"))

  //take texts
  override def fit(dataset: Dataset[_]): TopicCoherenceModel = new TopicCoherenceModel(dataset)

  override def copy(extra: ParamMap): Estimator[TopicCoherenceModel] = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = schema.add(name="topicCoherence", dataType=DataTypes.DoubleType, nullable = false)
}
