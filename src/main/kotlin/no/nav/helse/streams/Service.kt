package no.nav.helse.streams

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.prometheus.client.*
import io.prometheus.client.exporter.common.*
import io.prometheus.client.hotspot.*
import no.nav.helse.*
import org.apache.kafka.streams.*
import org.slf4j.*
import java.util.*

abstract class Service(val env: Environment) {
   protected abstract val SERVICE_APP_ID: String
   protected open val HTTP_PORT: Int = 8080

   private val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
   private val bootstrapServersConfig = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092"
   private val log = LoggerFactory.getLogger(SERVICE_APP_ID)

   private lateinit var streams: KafkaStreams

   fun start() {
      DefaultExports.initialize()
      naisHttpChecks()
      streams = setupStreams()
      streams.start()

      log.info("Started Service $SERVICE_APP_ID")
      addShutdownHook()
   }

   private fun naisHttpChecks() {
      embeddedServer(Netty, HTTP_PORT) {
         routing {
            get("/isAlive") {
               call.respondText("ALIVE", ContentType.Text.Plain)
            }
            get("/isReady") {
               call.respondText("READY", ContentType.Text.Plain)
            }
            get("/metrics") {
               val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()
               call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                  TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
               }
            }
         }
      }.start(wait = false)
   }

   fun stop() {
      streams.close()
   }

   // Override and extend the set of properties when needed
   open fun getConfig(): Properties {
      return streamConfig(SERVICE_APP_ID, bootstrapServersConfig, env)
   }

   private fun addShutdownHook() {
      Thread.currentThread().setUncaughtExceptionHandler { _, _ -> stop() }
      Runtime.getRuntime().addShutdownHook(Thread {
         stop()
      })
   }

   protected abstract fun setupStreams(): KafkaStreams

}
