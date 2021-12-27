import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sksamuel.hoplite.ConfigLoader
import org.jsoup.Jsoup
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


data class SessionConfig(val login: Login, val server: Server)
data class Login(val user: String, val pw: String)
data class Server(val host: String, val login: String, val dashboard: String) {
    val loginURL get() = host + login
    val dashboardURL get() = host + dashboard
}

class Session {
    private val cfg = ConfigLoader().loadConfigOrThrow<SessionConfig>("/application.yaml")
    private val login = cfg.login
    private val server = cfg.server
    private val cookie = getSession()

    val stores: List<Store>
        get() {
            val soup = Jsoup.parse(server.dashboardURL.getHtml(cookie))
            return soup.select("a[href~=fsbetrieb]")
                .map { Store(it.html(), it.attr("href")) }.toList()
        }

    /**
     * @param limitDate only return savers active before this date
     */
    fun getSaversIn(store: Store, limitDate: LocalDate): Set<Pair<Saver, LocalDate>> {
        val soup = Jsoup.parse((server.host + store.url).getHtml(cookie))
        return soup.select("#vue-storeteam").attr("data-vue-props")
            .let { Gson().fromJson(it.toString(), JsonObject::class.java).get("team").asJsonArray }
            .map { it.asJsonObject }
            .filter {
                it.get("last_fetch").tryGetDate?.let { it > limitDate } ?: false
            }
            .map {
                Saver(it.get("name").asString, it.get("id").asInt) to (it.get("last_fetch").tryGetDate
                    ?: LocalDate.ofEpochDay(0L))
            }.toSet()
    }

    private fun getSession(): String {
        val (_, response, _) = server.loginURL.httpPost()
            .jsonBody(Gson().toJson(login.let { mapOf("email" to it.user, "password" to it.pw) }))
            .response()
        return response.headers["Set-Cookie"].first()
    }
}

private fun String.getHtml(cookie: String): String {
    return this.httpGet().header(Headers.COOKIE to cookie).response().second.html()
}

private fun Response.html(): String {
    return body().asString(headers[Headers.CONTENT_TYPE].lastOrNull())
}


private val JsonElement.tryGetDate: LocalDate?
    get() {
        return if (!this.isJsonNull)
            Instant.ofEpochMilli(asLong * 1000).atZone(ZoneId.systemDefault()).toLocalDate()
        else
            null
    }