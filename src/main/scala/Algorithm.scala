package com.test

import io.prediction.controller.P2LAlgorithm
import io.prediction.controller.Params

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import grizzled.slf4j.Logger

case class AlgorithmParams(mult: Int) extends Params

class Algorithm(val ap: AlgorithmParams)
  // extends PAlgorithm if Model contains RDD[]
  extends P2LAlgorithm[PreparedData, Model, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, data: PreparedData): Model = {

    // Sort RDD of Posts by highest likeCount
    // and convert data to array
    val sortedPosts = data.events.sortBy(_._2.likeCount, false).collect

    new Model(postsArray = sortedPosts)

  }

  def predict(model: Model, query: Query): PredictedResult = {

    // Require some initial posts data to make predictions
    require(!model.postsArray.isEmpty, s"No training data available. Add some to Event Server and rerun pio build, train, deploy sequence in order to predict.")

    // Store the number of posts and optional query limit in variables
    // Note query.limit is of type Option[Int] so handle accordingly
    val numPosts = model.postsArray.size
    val returnLimit = query.limit getOrElse numPosts

    // Require limit parameter to be positive
    require(returnLimit >= 1, s"limit parameter must be an integer greater than 0")

    // Print a warning message in case too much data is requested
    val maxLimit = 20
    if (returnLimit >= maxLimit) {
      println("WARNING: Limit parameter shouln\'t be set too high, as to avoid large return sets")
    }

    // Handle optinal lastPostId parameter and find in data
    val lastPostId = query.lastPostId getOrElse 0
    val lastPostIdIndex = model.postsArray.indexWhere(_._1.toInt == lastPostId)

    // Distinguish between passed lastPostId or not
    // This will be used to indentify unknown postId from no passed id
    var lastPostIdSet: Boolean = false
    query.lastPostId match {
      case Some(id) => lastPostIdSet = true
      case None => lastPostIdSet = false
    }

    // Grab the needed part of our data and store in as returnArray
    val returnArray = model.postsArray.slice(from = lastPostIdIndex + 1, until = lastPostIdIndex + 1 + returnLimit)

    // Map each Post Object to a likeCount and create necessary PostScore objects
    val postLikesArray = returnArray.map{ case (id, post) =>
      new PostScore(
        postId = id.toInt,
        likeCount = post.likeCount
      )
    }

    // Return data or none if lastPostId is unknown
    if (lastPostIdSet && lastPostIdIndex == -1) {
      println("No such lastPostId exists in the database")
      PredictedResult(postScores = Array())
    } else {
      PredictedResult(postScores = postLikesArray)
    }

  }
}

class Model(
  val postsArray: Array[(String, Post)]
) extends Serializable {
  override def toString = s"${postsArray.size} posts in model"
}