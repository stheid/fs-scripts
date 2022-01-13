import logic.Service
import logic.getSavers
import java.time.LocalDate

class InactiveService(session: Session, cfg: Config) : Service(session, cfg) {
    fun getInactive(): String {
        val today = LocalDate.now()
        val observationWindow = today.minusMonths(6)

        val stores = session.stores.filter { !cfg.stores.exclude.contains(it.id) }.associateWith { store ->
            session.getSaversIn(store)
                // only keep pickups older than observation winows
                .filter { (_, lastDate) -> lastDate?.let { it < observationWindow } == true }
        }

        val inactiveSavers = stores.getSavers()

        return inactiveSavers.toSortedSet(compareBy { it.name }).joinToString("\n")
    }
}