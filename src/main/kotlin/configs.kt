data class Config(val server: Server, val stores: Stores)
data class Server(val host: String, val login: String, val dashboard: String) {
    val loginURL get() = host + login
    val dashboardURL get() = host + dashboard
}

data class Stores(val fairteilers: Set<Int>, val exclude: Set<Int>)


data class Credentials(val user: String, val pw: String)
