package logic

import Saver
import java.time.LocalDate


internal fun <K> Map<K, Collection<Triple<Saver, LocalDate?, LocalDate?>>>.getSavers(
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
