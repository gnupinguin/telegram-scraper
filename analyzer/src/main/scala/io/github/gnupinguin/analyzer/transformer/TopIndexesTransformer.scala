package io.github.gnupinguin.analyzer.transformer

import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.types.{DataType, DataTypes}

class TopIndexesTransformer(override val uid: String, val topCount: Int, val minValue: Double) extends UnaryTransformer[Vector, Array[Int], TopIndexesTransformer]{

  def this(topCount: Int, minValue: Double) = this(Identifiable.randomUID("top-indexes-transformer"), topCount, minValue)

  override protected def createTransformFunc: Vector => Array[Int] = { v =>
    val top = v.toDense.values.zipWithIndex
      .sortBy(_._1).reverse
      .take(topCount)
    val filtered = top.filter(_._2 >= minValue)

    if (!filtered.isEmpty) {
      filtered.map(_._2)
    } else if (!top.isEmpty) {
      top.take(1).map(_._2)
    } else {
      Array[Int]()
    }
  }

  override protected def outputDataType: DataType = DataTypes.createArrayType(DataTypes.IntegerType)

}
