data class Store(val name: String, val url: String) {
    val id get() = Regex("\\d+$").find(url)!!.value.toInt()
}

data class Saver(val name: String, val id: Int) {
    override fun toString(): String {
        return "${name} (${id})"
    }
}