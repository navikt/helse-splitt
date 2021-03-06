package no.nav.helse

data class Environment(
   val username: String = getRequiredEnvVar("KAFKA_USERNAME"),
   val password: String = getRequiredEnvVar("KAFKA_PASSWORD"),
   val bootstrapServersUrl: String = getRequiredEnvVar("KAFKA_BOOTSTRAP_SERVERS"),
   val httpPort: Int? = null,
   val navTruststorePath: String? = getEnvVar("NAV_TRUSTSTORE_PATH"),
   val navTruststorePassword: String? = getEnvVar("NAV_TRUSTSTORE_PASSWORD")
   )

private fun getRequiredEnvVar(varName: String) =
   getEnvVar(varName) ?: getSystemProperty(varName) ?:
      throw RuntimeException("Missing required variable \"$varName\"")

private fun getEnvVar(varName: String) = System.getenv(varName) ?: null

private fun getSystemProperty(varName: String) = System.getProperty(varName)
