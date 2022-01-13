package logic

import Config
import Session
import Store
import java.time.LocalDate

class CheckService(session: Session, cfg: Config) : Service(session, cfg) {
    fun getUnhelpfulFoodsavers(): String {
        val fairteiler = cfg.stores.fairteilers
        val today = LocalDate.now()
        val inactiveDate = today.minusMonths(2)
        val observationWindow = today.minusMonths(6)

        fun Store.isFairteiler(): Boolean {
            return fairteiler.contains(this.id)
        }

        val stores = session.stores.filter { !cfg.stores.exclude.contains(it.id) }.associateWith { store ->
            session.getSaversIn(store)
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

        return criticalIndividuals.toSortedMap(compareBy { it.name }).map { "${it.key} -> ${it.value}" }
            .joinToString("\n")
    }
}
