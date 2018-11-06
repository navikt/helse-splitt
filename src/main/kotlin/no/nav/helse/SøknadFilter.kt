package no.nav.helse

import no.nav.helse.streams.*
import no.nav.helse.streams.Topics.SYKEPENGESØKNADER_INN
import org.apache.kafka.streams.*
import org.slf4j.*


class SøknadFilter {

   private val appId = "sykepengesoknad-filter"
   private val env: Environment = Environment()

   private val log = LoggerFactory.getLogger("SøknadFilter")

   fun start() {
      StreamConsumer(appId, Environment(), søknader()).start()
   }

   private fun søknader(): KafkaStreams {
      val builder = StreamsBuilder()

      builder.consumeTopic(SYKEPENGESØKNADER_INN)
         .peek { key, value -> log.info("Processing ${value.javaClass} with key $key") }

      return KafkaStreams(builder.build(), streamConfig(appId, env))
   }

}
