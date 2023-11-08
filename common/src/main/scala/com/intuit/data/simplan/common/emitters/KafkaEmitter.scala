package com.intuit.data.simplan.common.emitters

import com.intuit.data.simplan.common.config.SimplanEmitterConfig
import com.intuit.data.simplan.global.utils.ExecutionUtils.retrySafely
import com.intuit.data.simplan.global.utils.SimplanImplicits._
import com.intuit.data.simplan.logging.Logging
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 04-Aug-2022 at 9:19 AM
  */

case class KafkaEmitterConfig(producerConfig: Map[String, String], topic: Option[String], maxRetries: Integer, retryInterval: Long) extends Serializable

class KafkaEmitter(emitterConfig: SimplanEmitterConfig) extends SimplanEmitter(emitterConfig) with Logging {

  private val kafkaEmitterConfig: KafkaEmitterConfig = parseConfigAs[KafkaEmitterConfig]
  private val producer = new KafkaProducer[String, String](kafkaEmitterConfig.producerConfig.toProperties)

  override def emitInternal(message: String, topic: Option[String] = None, keyOption: Option[String] = None): Boolean = {
    val resolvedTopic = Option(topic.getOrElse(kafkaEmitterConfig.topic.orNull))
    if (resolvedTopic.isDefined) {
      retrySafely(kafkaEmitterConfig.maxRetries, kafkaEmitterConfig.retryInterval, message = s"KafkaEmitter: Sending message to $resolvedTopic failed.") {
        keyOption match {
          case Some(key) => producer.send(new ProducerRecord[String, String](resolvedTopic.get, key, message)).get()
          case None      => producer.send(new ProducerRecord(resolvedTopic.get, message)).get()
        }
      }.isSuccess
    } else {
      logger.warn("Topic not defined for Kafka Emitter. Nothing produced")
      false
    }
  }
}
