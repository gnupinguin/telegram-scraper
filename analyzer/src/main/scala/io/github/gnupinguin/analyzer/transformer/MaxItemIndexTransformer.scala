package io.github.gnupinguin.analyzer.transformer

import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.types.{DataType, DataTypes}

class MaxItemIndexTransformer(override val uid: String) extends UnaryTransformer[Vector, Int, MaxItemIndexTransformer]{

  def this() = this(Identifiable.randomUID("max-item-index-transformer"))

  override protected def createTransformFunc: Vector => Int = _.toDense
    .values
    .zipWithIndex
    .maxBy(_._1)._2

  override protected def outputDataType: DataType = DataTypes.IntegerType

}
