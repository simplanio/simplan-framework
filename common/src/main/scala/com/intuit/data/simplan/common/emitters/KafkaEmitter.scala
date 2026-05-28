package com.intuit.data.simplan.common.emitters

import com.intuit.data.simplan.common.config.SimplanEmitterConfig
import com.intuit.data.simplan.global.utils.ExecutionUtils.retrySafely
import com.intuit.data.simplan.global.utils.SimplanImplicits._
import com.intuit.data.simplan.logging.Logging
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

/**
 * @author Abraham, Thomas - tabraham1
 *         Created on 04-Aug-2022 at 9:19 AM */
case class KafkaEmitterConfig(producerConfig: Map[String, String], topic: Option[String], maxRetries: Integer, retryInterval: Long, isBlocking: Boolean = false) extends Serializable

class KafkaEmitter(emitterConfig: SimplanEmitterConfig) extends SimplanEmitter(emitterConfig) with Logging {

  private val kafkaEmitterConfig: KafkaEmitterConfig = parseConfigAs[KafkaEmitterConfig]
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z").withZone(ZoneId.of("UTC"))
  private val producer = {
    logger.info("Creating Kafka Producer with config: {}", kafkaEmitterConfig.producerConfig)
    logger.info("Starting the producer with isBlocking: {}", kafkaEmitterConfig.isBlocking)
    new KafkaProducer[String, String](kafkaEmitterConfig.producerConfig.toProperties)
  }

  override def emitInternal(message: String, topic: Option[String] = None, keyOption: Option[String] = None): Boolean = {
    val resolvedTopic = Option(topic.getOrElse(kafkaEmitterConfig.topic.orNull))
    if (resolvedTopic.isDefined) {
      retrySafely(kafkaEmitterConfig.maxRetries, kafkaEmitterConfig.retryInterval, message = s"KafkaEmitter: Sending message to $resolvedTopic failed.") {
        logger.info(s"Trying to Send message to $resolvedTopic")
        keyOption match {
          case Some(key) => logger.info("\n[\nEvent time : {} \nTopic : {} \nKey (Run Id) : {} \nValue :  {} \n]", getFormattedCurrentDate, resolvedTopic.get, key, message)

            val record = new ProducerRecord[String, String](resolvedTopic.get, key, message)
            val recordSent = producer.send(record)
            if (kafkaEmitterConfig.isBlocking) {
              val recordSentDetails = recordSent.get()
              logger.info("Record sent to topic {} with offset {} and partition {}",
                recordSentDetails.topic(), recordSentDetails.offset().toString, recordSentDetails.partition().toString)

            }
          case None => val recordSent = producer.send(new ProducerRecord(resolvedTopic.get, message))
            if (kafkaEmitterConfig.isBlocking){
              val recordSentDetails = recordSent.get()
              logger.info("Record sent to topic {} with offset {} and partition {}",
                recordSentDetails.topic(), recordSentDetails.offset().toString, recordSentDetails.partition().toString)
            }
        }
      }.isSuccess
    } else {
      logger.warn("Topic not defined for Kafka Emitter. Nothing produced")
      false
    }
  }

  private def getFormattedCurrentDate = {
    val localDateTime = LocalDateTime.now
    val zonedDateTime = localDateTime.atZone(dateTimeFormatter.getZone)
    val formattedDateString = dateTimeFormatter.format(zonedDateTime)
    val zone = dateTimeFormatter.getZone.getId
    String.format("%s %s", formattedDateString, zone)
  }
}
