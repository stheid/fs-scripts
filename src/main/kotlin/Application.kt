import com.sksamuel.hoplite.ConfigLoader
import java.time.LocalDate
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.lang.RuntimeException


fun getUnhelpfulFoodsavers(): String {
    val user: String = System.getenv("USER") ?: throw RuntimeException("USER not provided")
    val pw: String = System.getenv("PWD") ?: throw RuntimeException("PWD not provided")

    val login = Credentials(user, pw)
    val cfg = ConfigLoader().loadConfigOrThrow<Config>("/application.yaml")

    val fairteiler = cfg.stores.fairteilers
    val sess = Session(login, cfg.server)
    val today = LocalDate.now()
    val sixMonthEarlier = today.minusMonths(6)

    val stores = sess.stores
        .filter { !cfg.stores.exclude.contains(it.id) }
        .associateWith { sess.getSaversIn(it, sixMonthEarlier) }

    val leechers = (stores.filter { !fairteiler.contains(it.key.id) }.toSetOfSavers() // stores
            - stores.filter { fairteiler.contains(it.key.id) }.toSetOfSavers())
        .associateWith { leecher ->
            stores.filter { it.value.map { (saver, _) -> saver }.contains(leecher) }
                .map { (store, entry) -> "${store.name} ${entry.single { (saver, _) -> saver == leecher }.second}" }
        }

    return leechers.map { "${it.key} -> ${it.value}" }.joinToString("\n")
}

fun main() {
    val path: String = System.getenv("SECRET") ?: throw RuntimeException("SECRET not provided")
    embeddedServer(Netty, port = 80) {
        routing {
            get("/${path}/check") {
                log.info("responding to request /${path}")
                call.respondText(getUnhelpfulFoodsavers())
            }
        }
    }.start(wait = true)
}

private fun <K, V> Map<K, Collection<Pair<Saver, V>>>.toSetOfSavers(): Set<Saver> {
    return this.values.flatten().map { it.first }.toSet()
}
