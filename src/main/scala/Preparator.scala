package com.test

import io.prediction.controller.PPreparator
import io.prediction.data.storage.Event

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

class Preparator
  extends PPreparator[TrainingData, PreparedData] {

  // pass all TrainingData as PreparedData.
  // no need to do anything else here

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    new PreparedData(events = trainingData.events)
  }
}

class PreparedData(
  val events: RDD[(String, Post)]
) extends Serializable