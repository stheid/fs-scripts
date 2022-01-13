import com.sksamuel.hoplite.ConfigLoader
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import logic.CheckService
import java.lang.RuntimeException


fun main() {
    val secret: String = System.getenv("SECRET") ?: throw RuntimeException("SECRET not provided")
    val user: String = System.getenv("USER") ?: throw RuntimeException("USER not provided")
    val pw: String = System.getenv("PW") ?: throw RuntimeException("PW not provided")

    val login = Credentials(user, pw)
    val cfg = ConfigLoader().loadConfigOrThrow<Config>("/application.yaml")

    val sess = Session(login, cfg.server)

    embeddedServer(Netty, port = 80) {
        routing {
            get("/check/${secret}") {
                log.info("responding to request /check/${secret}")
                call.respondText(CheckService(sess, cfg).getUnhelpfulFoodsavers())
            }
            get("/inactive/${secret}") {
                log.info("responding to request /InactiveService.kt/${secret}")
                val inactiveMonth = call.request.queryParameters.get("month")?.toIntOrNull()
                call.respondText(InactiveService(sess, cfg).getInactive(inactiveMonth))
            }
        }
    }.start(wait = true)
}
