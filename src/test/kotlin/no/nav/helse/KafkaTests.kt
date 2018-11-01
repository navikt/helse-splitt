package no.nav.helse

import org.amshove.kluent.*
import org.apache.kafka.clients.producer.*
import org.jetbrains.spek.api.*
import org.jetbrains.spek.api.dsl.*
import org.testcontainers.containers.*
import java.time.*
import java.util.*

object KafkaTests: Spek({

   val kafka: KafkaContainer = KafkaContainer("5.0.0")
      .withEmbeddedZookeeper()
      .withStartupTimeout(Duration.ofSeconds(60))

   beforeGroup {
      kafka.start()
   }

   group("tests requiring a running Kafka cluster") {

         it("sends msgs to a topic synchronously") {
            val props = Properties().apply {
               put("bootstrap.servers", kafka.bootstrapServers)
               put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
               put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            }

            KafkaProducer<String, String>(props).use { producer ->
               for (i in 1..100) {
                  producer.send(ProducerRecord("my-topic", "this is a test $i")).get()
               }
               val sendCount = producer.metrics()
                  .filterKeys { it.name() == "record-send-total" }
                  .values
                  .first()
               sendCount.metricValue() `should equal` 100.0
            }

      }

   }

   afterGroup {
      kafka.stop()
   }

})
