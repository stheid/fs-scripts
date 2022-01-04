import com.sksamuel.hoplite.ConfigLoader
import java.lang.RuntimeException
import java.time.LocalDate

fun getUnhelpfulFoodsavers(): String {
    val user: String = System.getenv("USER") ?: throw RuntimeException("USER not provided")
    val pw: String = System.getenv("PW") ?: throw RuntimeException("PW not provided")

    val login = Credentials(user, pw)
    val cfg = ConfigLoader().loadConfigOrThrow<Config>("/application.yaml")

    val fairteiler = cfg.stores.fairteilers
    val sess = Session(login, cfg.server)
    val today = LocalDate.now()

    fun Store.isFairteiler(): Boolean {
        return fairteiler.contains(this.id)
    }

    val stores = sess.stores.filter { !cfg.stores.exclude.contains(it.id) }.associateWith { store ->
        if (store.isFairteiler()) sess.getSaversIn(store) { fetchDate -> fetchDate > today.minusMonths(6) }
        else sess.getSaversIn(store) { fetchDate ->
            // if foodsaver was active in the 2 month, but did not make its cleaning
            fetchDate > today.minusMonths(6) && fetchDate < today.minusMonths(2)
        }
    }

    val criticalIndividuals = (stores.filter { !it.key.isFairteiler() }.toSetOfSavers() // stores
            - stores.filter { it.key.isFairteiler() }.toSetOfSavers()).associateWith { leecher ->
        stores.filter { it.value.map { (saver, _) -> saver }.contains(leecher) }
            .map { (store, entry) -> "${store.name} ${entry.single { (saver, _) -> saver == leecher }.second}" }
    }

    return criticalIndividuals.toSortedMap(compareBy { it.name }).map { "${it.key} -> ${it.value}" }.joinToString("\n")
}

private fun <K, V> Map<K, Collection<Pair<Saver, V>>>.toSetOfSavers(): Set<Saver> {
    return this.values.flatten().map { it.first }.toSet()
}


fun main() {
    println(getUnhelpfulFoodsavers())
}