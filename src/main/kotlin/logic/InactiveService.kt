import logic.Service
import logic.getSavers
import java.time.LocalDate

class InactiveService(session: Session, cfg: Config) : Service(session, cfg) {
    fun getInactive(inactiveMonth: Int? = null): String {
        val today = LocalDate.now()
        val inactiveDate = today.minusMonths((inactiveMonth ?: 6).toLong())

        val stores = session.stores
            .filter { !cfg.stores.exclude.contains(it.id) }
            .associateWith { store -> session.getSaversIn(store) }

        val allSavers = stores.getSavers()
        val activeSavers = stores.getSavers({ it > inactiveDate })

        // inactive savers
        return (allSavers - activeSavers).toSortedSet(compareBy { it.name }).joinToString("\n")
    }
}