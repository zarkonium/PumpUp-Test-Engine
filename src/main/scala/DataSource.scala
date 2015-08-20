package com.test

import io.prediction.controller.PDataSource
import io.prediction.controller.EmptyEvaluationInfo
import io.prediction.controller.EmptyActualResult
import io.prediction.controller.Params
import io.prediction.data.storage.Event
import io.prediction.data.store.PEventStore

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import grizzled.slf4j.Logger

case class DataSourceParams(appName: String) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {

    // create a RDD of (entityID, Post)
    val eventsRDD: RDD[(String, Post)] = PEventStore.aggregateProperties(
      appName = dsp.appName,
      entityType = "post"
    )(sc).map { case (entityId, properties) =>
      val post = try {
        Post(likeCount = properties.get[Int]("likeCount"))
      } catch {
        case e: Exception => {
          logger.error(s"Failed to get properties ${properties} of" +
            s" postr ${entityId}. Exception: ${e}.")
          throw e
        }
      }
      (entityId, post)
    }.cache()


    new TrainingData(eventsRDD)
  }
}

// A Post is an object with a single property,
// that being the likeCount
case class Post(likeCount: Int)

class TrainingData(
  val events: RDD[(String, Post)]
) extends Serializable {
  override def toString = {
    s"events: [${events.count()}] (${events.take(2).toList}...)"
  }
}