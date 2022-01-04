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
    val observationWindow = today.minusMonths(6)

    fun Store.isFairteiler(): Boolean {
        return fairteiler.contains(this.id)
    }

    val stores = sess.stores.filter { !cfg.stores.exclude.contains(it.id) }.associateWith { store ->
        sess.getSaversIn(store)
            // only keep pickups in the observation window
            .filter { (_, lastDate) -> lastDate?.let { it > observationWindow } == true }
    }

    val saversActiveInStores =
        stores.filter { !it.key.isFairteiler() }.getSavers(
            // did recently pickup
            { it > inactiveDate },
            // is foodsaver for a while
            { it < inactiveDate })
    val saversInFairteilers = stores.filter { it.key.isFairteiler() }.getSavers()

    // mapping of foodsavers to list of his latest pickups per store
    val criticalIndividuals = (saversActiveInStores - saversInFairteilers)
        .associateWith { saver ->
            stores
                // stores that contain the saver
                .filter { (_, savers) -> savers.any { (saver_, _) -> saver_ == saver } }
                // as a list of (store, date), where date is the date, this saver did the last pickup
                .map { (store, entry) -> store.name to entry.single { (saver_, _) -> saver_ == saver }.second }
                .sortedByDescending { (_, lastDate) -> lastDate }
                .map { "${it.first} ${it.second}" }
        }

    return criticalIndividuals.toSortedMap(compareBy { it.name }).map { "${it.key} -> ${it.value}" }.joinToString("\n")
}


private fun <K> Map<K, Collection<Triple<Saver, LocalDate?, LocalDate?>>>.getSavers(
    lastPickupFilter: (LocalDate) -> Boolean = { true },
    addDateFilter: (LocalDate) -> Boolean = { true }
): Set<Saver> {
    return this.values.flatten()
        .filter { (_, lastDate, addDate) ->
            lastDate?.let(lastPickupFilter) ?: false
                    && addDate?.let(addDateFilter) ?: false
        }
        .map { it.first }
        .toSet()
}

fun main() {
    println(getUnhelpfulFoodsavers())
}