import java.time.LocalDate


fun main() {
    val fairteiler = setOf(4089, 30932)
    val sess = Session()
    val today = LocalDate.now()
    val sixMonthEarlier = today.minusMonths(6)

    val stores = sess.stores.associateWith { sess.getSaversIn(it, sixMonthEarlier) }

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
