package no.nav.helse

import io.prometheus.client.*
import no.nav.helse.streams.*
import no.nav.helse.streams.Topics.SYKEPENGESØKNADER_INN
import no.nav.helse.streams.Topics.SYKEPENGESØKNADER_UT
import org.apache.kafka.streams.*
import org.slf4j.*


class SøknadFilter {

   private val appId = "sykepengesoknad-filter"
   private val env: Environment = Environment()
   private val log = LoggerFactory.getLogger("SøknadFilter")

   private val counter = Counter.build()
      .name("sykepenger_mottatte_soknader")
      .help("Antall mottatte søknader til filtrering")
      .register()

   fun start() {
      StreamConsumer(appId, Environment(), søknader()).start()
   }

   private fun søknader(): KafkaStreams {
      return KafkaStreams(søknadStreamsBuilder().build(), streamConfig(appId, env))
   }

   internal fun søknadStreamsBuilder(): StreamsBuilder {
      val builder = StreamsBuilder()
      builder.consumeTopic(SYKEPENGESØKNADER_INN)
         .peek { key, value -> log.info("Processing ${value.javaClass} with key $key") }
         .peek { _, _ -> counter.inc() }
         .toTopic(SYKEPENGESØKNADER_UT)

      return builder
   }

}
