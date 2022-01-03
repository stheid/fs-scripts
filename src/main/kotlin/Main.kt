import com.sksamuel.hoplite.ConfigLoader
import java.time.LocalDate


fun main() {
    val cfg = ConfigLoader().loadConfigOrThrow<Config>("/application.yaml")
    val login = ConfigLoader().loadConfigOrThrow<Credentials>("/credencials.yaml")

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

    leechers.forEach { println("${it.key} -> ${it.value}") }
}

private fun <K, V> Map<K, Collection<Pair<Saver, V>>>.toSetOfSavers(): Set<Saver> {
    return this.values.flatten().map { it.first }.toSet()
}
