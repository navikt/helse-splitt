package no.nav.helse

fun main() {
   println("Available envs: ${System.getenv().keys}")
   SøknadFilter().start()
}
