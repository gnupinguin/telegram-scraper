package io.github.gnupinguin.analyzer.estimator

import org.apache.spark.sql.Encoder
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.expressions.Aggregator

case class TopicHeightTriangleMatrix(pairs: Array[Array[(Int, Int)]])

object TopicPairsAggregator extends Aggregator[Array[Array[(Int, Int)]], TopicHeightTriangleMatrix, Array[Array[(Int, Int)]]] {

  override def zero: TopicHeightTriangleMatrix = TopicHeightTriangleMatrix(Array())

  override def reduce(b: TopicHeightTriangleMatrix, a: Array[Array[(Int, Int)]]): TopicHeightTriangleMatrix = {
    TopicHeightTriangleMatrix(aggregateTopics(b.pairs, a))
  }

  override def merge(b1: TopicHeightTriangleMatrix, b2: TopicHeightTriangleMatrix): TopicHeightTriangleMatrix = {
    TopicHeightTriangleMatrix(aggregateTopics(b1.pairs, b2.pairs))
  }

  override def finish(reduction: TopicHeightTriangleMatrix): Array[Array[(Int, Int)]] = reduction.pairs

  override def bufferEncoder: Encoder[TopicHeightTriangleMatrix] = ExpressionEncoder()

  override def outputEncoder: Encoder[Array[Array[(Int, Int)]]] = encoderIn

  private def aggregateTopics(topics1: Array[Array[(Int, Int)]], topics2: Array[Array[(Int, Int)]]): Array[Array[(Int, Int)]] = {
    (topics1, topics2) match {
      case (Array(), b) => b
      case (a, Array()) => a
      case _ => topics1.zip(topics2).map {
        case (a1, a2) => a1.zip(a2).map {
          case ((x1, y1), (x2, y2)) => (x1 + x2, y1 + y2)
        }
      }
    }
  }

  def encoderIn: Encoder[Array[Array[(Int, Int)]]] = ExpressionEncoder()


//    new Encoder[Array[Array[(Int, Int)]]] {
//
//    override def schema: StructType =
//      DataTypes.createStructType(Array(StructField("pairs",
//      DataTypes.createArrayType(
//        DataTypes.createArrayType(
//          DataTypes.createStructType(
//            Array(
//              StructField("_1", DataTypes.IntegerType),
//              StructField("_2", DataTypes.IntegerType)
//            )
//          ))))))
//
//    override def clsTag: ClassTag[Array[Array[(Int, Int)]]] = classTag[Array[Array[(Int, Int)]]]
//  }
//    Encoders.bean[Array[Array[(Int, Int)]]]
//    Encoders.bean(Array(Array((1,1))).getClass.asInstanceOf[Class[Array[Array[(Int, Int)]]]])

}
