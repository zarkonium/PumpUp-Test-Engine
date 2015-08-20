package com.test

import io.prediction.controller.IEngineFactory
import io.prediction.controller.Engine

case class Query(
  limit: Option[Int],
  lastPostId: Option[Int]
) extends Serializable

// PredictedResults is the return object and its made of
// an array of PostScore objects
case class PredictedResult(
  postScores: Array[PostScore]
) extends Serializable

// PostScore will be an object which holds a postId and
// its likeCount which in this case is used as a "score"
case class PostScore(
  postId: Int,
  likeCount: Int
) extends Serializable

object PumpUpEngine extends IEngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("algo" -> classOf[Algorithm]),
      classOf[Serving])
  }
}