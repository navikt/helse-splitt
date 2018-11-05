package no.nav.helse

import kotlinx.coroutines.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.*
import org.apache.kafka.streams.*
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

            runBlocking {
               val props = Properties().apply {
                  put("bootstrap.servers", kafka.bootstrapServers)
                  put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                  put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                  put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-test-jk")
                  put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
                  put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
                  put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
               }

               KafkaProducer<String, String>(props).use { producer ->
                  val sender = launch {
                     while (true) {
                        val randomNr = (Math.random() * 1E6).toInt()
                        producer.send(ProducerRecord("my-topic", "key $randomNr", "value ${randomNr}"))
                        producer.flush()
                        println("sent $randomNr")
                        delay(1000)
                     }
                  }

                  val builder = StreamsBuilder()
                  builder.stream<String, String>("my-topic")
                     .peek { key, value -> println("received $key -> $value") }
                     .to("output")
                  val topology = builder.build()
                  KafkaStreams(topology, props).start()
                  println(topology.describe())


                  sender.join()
               }
            }

      }

   }

   afterGroup {
      kafka.stop()
   }

})
