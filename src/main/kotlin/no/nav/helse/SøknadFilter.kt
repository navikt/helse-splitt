package no.nav.helse

import no.nav.helse.streams.*
import no.nav.helse.streams.Topics.SYKEPENGESØKNADER_INN
import org.apache.kafka.streams.*
import org.slf4j.*
import java.util.*


class SøknadFilter: Service(Environment()) {
   override val SERVICE_APP_ID = "sykepengesoknad-filter" // NB: also used as group.id for the consumer group - do not change!
   override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT

   private val log = LoggerFactory.getLogger(SERVICE_APP_ID)

   override fun setupStreams(): KafkaStreams {
      val builder = StreamsBuilder()

      builder.consumeTopic(SYKEPENGESØKNADER_INN)
         .peek { key, value -> log.info("Processing ${value.javaClass} with key $key") }

      return KafkaStreams(builder.build(), this.getConfig())
   }

   override fun getConfig(): Properties {
      return streamConfig(
         appId = SERVICE_APP_ID,
         bootStapServerUrl = env.bootstrapServersUrl,
         env = env
      )
   }

}
