package no.nav.helse

import no.nav.helse.streams.Topics.SYKEPENGESØKNADER_INN
import no.nav.helse.streams.Topics.SYKEPENGEBEHANDLING
import org.amshove.kluent.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.test.*
import org.jetbrains.spek.api.*
import org.jetbrains.spek.api.dsl.*
import org.json.*
import java.util.*

object StreamTest: Spek({

   val consumerFactory = ConsumerRecordFactory<String, JSONObject>(
      SYKEPENGESØKNADER_INN.name,
      SYKEPENGESØKNADER_INN.keySerde.serializer(),
      SYKEPENGESØKNADER_INN.valueSerde.serializer()
   )

   describe("testing a topology") {

      beforeGroup {
         System.setProperty("KAFKA_USERNAME", "bogus")
         System.setProperty("KAFKA_PASSWORD", "bogus")
         System.setProperty("KAFKA_BOOTSTRAP_SERVERS", "bogus")
         System.setProperty("KAFKA_SCHEMA_REGISTRY_URL", "bogus")
      }

      given("topology description from the SøknadFilter app") {
         it("is able to pass a message through"){
            val config = Properties().apply {
               put(StreamsConfig.APPLICATION_ID_CONFIG, "test")
               put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")
            }

            val builder = SøknadFilter().søknadStreamsBuilder()
            val input = JSONObject().append("soknadstype", "el typo").append("status", "opp og avgjort")
            TopologyTestDriver(builder.build(), config).use { testDriver ->
               testDriver.pipeInput(consumerFactory.create(SYKEPENGESØKNADER_INN.name, "nykkel", input))
               val outputRecord = testDriver.readOutput(
                  SYKEPENGEBEHANDLING.name,
                  SYKEPENGEBEHANDLING.keySerde.deserializer(),
                  SYKEPENGEBEHANDLING.valueSerde.deserializer()
               )
               val output = outputRecord.value()
               output.toMap() `should equal` input.toMap()
            }
         }
      }
   }

})
