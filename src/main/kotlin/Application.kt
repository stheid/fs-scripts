import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.lang.RuntimeException


fun main() {
    val path: String = System.getenv("SECRET") ?: throw RuntimeException("SECRET not provided")
    embeddedServer(Netty, port = 80) {
        routing {
            get("/check/${path}") {
                log.info("responding to request /check/${path}")
                call.respondText(getUnhelpfulFoodsavers())
            }
        }
    }.start(wait = true)
}
