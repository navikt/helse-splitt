package no.nav.helse

import io.prometheus.client.*
import no.nav.helse.streams.*
import no.nav.helse.streams.Topics.SYKEPENGESØKNADER_INN
import no.nav.helse.streams.Topics.SYKEPENGEBEHANDLING
import org.apache.kafka.streams.*
import org.slf4j.*


class SøknadFilter {

   private val appId = "splitt"
   private val env: Environment = Environment()
   private val log = LoggerFactory.getLogger("SøknadFilter")

   private val counter = Counter.build()
      .name("sykepenger_mottatte_soknader")
      .labelNames("type", "status")
      .help("Antall mottatte søknader til filtrering")
      .register()

   fun start() {
      val env = Environment()
      StreamConsumer(appId, søknader(), env.httpPort ?: 8080).start()
   }

   private fun søknader(): KafkaStreams {
      return KafkaStreams(søknadStreamsBuilder().build(),
         streamConfig(appId,
            env.bootstrapServersUrl,
            Pair(env.username, env.password),
            Pair(env.navTruststorePath, env.navTruststorePassword)
         )
      )
   }

   internal fun søknadStreamsBuilder(): StreamsBuilder {
      val builder = StreamsBuilder()
      builder.consumeTopic(SYKEPENGESØKNADER_INN)
         .peek { key, value -> log.info("Processing ${value.javaClass} with key $key") }
         .peek { _, value -> counter.labels(value["soknadstype"].toString(), value["status"].toString()).inc() }
         .toTopic(SYKEPENGEBEHANDLING)

      return builder
   }

}
