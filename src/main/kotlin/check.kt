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
    val inactiveDate = today.minusMonths(2)
    val observationWindow = today.minusMonths(2)

    fun Store.isFairteiler(): Boolean {
        return fairteiler.contains(this.id)
    }

    val stores = sess.stores.filter { !cfg.stores.exclude.contains(it.id) }.associateWith { store ->
        sess.getSaversIn(store).filter { (_, lastDate) -> lastDate?.let { it > observationWindow } == true }
    }

    val saversActiveInStores = stores.filter { !it.key.isFairteiler() }.getSavers { it < inactiveDate }
    val saversInFairteilers = stores.filter { it.key.isFairteiler() }.getSavers()

    val criticalIndividuals = (saversActiveInStores - saversInFairteilers)
        .associateWith { leecher ->
            stores.filter { (store, savers) -> savers.map { (saver, _) -> saver }.contains(leecher) }
                .map { (store, entry) -> "${store.name} ${entry.single { (saver, _) -> saver == leecher }.second}" }
        }

    return criticalIndividuals.toSortedMap(compareBy { it.name }).map { "${it.key} -> ${it.value}" }.joinToString("\n")
}


private fun <K> Map<K, Collection<Pair<Saver, LocalDate?>>>.getSavers(filter: (LocalDate) -> Boolean = { true }): Set<Saver> {
    return this.values.flatten().filter { (_, lastDate) -> lastDate?.let(filter) == true }.map { it.first }.toSet()
}

fun main() {
    println(getUnhelpfulFoodsavers())
}