package no.nav.helse

import no.nav.helse.streams.Topics.SYKEPENGESØKNADER_INN
import no.nav.helse.streams.Topics.SYKEPENGESØKNADER_UT
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
         System.setProperty("SRVFILTER_USERNAME", "bogus")
         System.setProperty("SRVFILTER_PASSWORD", "bogus")
      }

      given("topology description from the SøknadFilter app") {
         it("is able to pass a message through"){
            val config = Properties().apply {
               put(StreamsConfig.APPLICATION_ID_CONFIG, "test")
               put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")
            }

            val builder = SøknadFilter().søknadStreamsBuilder()
            val input = JSONObject().append("somekey", "somevalue")
            TopologyTestDriver(builder.build(), config).use { testDriver ->
               testDriver.pipeInput(consumerFactory.create(SYKEPENGESØKNADER_INN.name, "nykkel", input))
               val outputRecord = testDriver.readOutput(
                  SYKEPENGESØKNADER_UT.name,
                  SYKEPENGESØKNADER_UT.keySerde.deserializer(),
                  SYKEPENGESØKNADER_UT.valueSerde.deserializer()
               )
               val output = outputRecord.value()
               output.toMap() `should equal` input.toMap()
            }
         }
      }
   }

})
