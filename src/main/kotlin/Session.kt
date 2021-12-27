import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import com.sksamuel.hoplite.ConfigLoader
import data.Store
import org.jsoup.Jsoup.*

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



    val firms: List<Store>
        get() {
            val (_,response,result) = server.dashboardURL.httpGet().header(Headers.COOKIE to getSession()).response()
            val soup = parse(response.html())
            // reading the firms from the data-vue-props
            return soup.select("a[href~=fsbetrieb]")
                .map {
                    println(it.attr("href") to it.html())
                Store()}.toList()
        }

    fun getSession(): String {

        val (_, response, _)  = server.loginURL.httpPost()
            .jsonBody( Gson().toJson(login.let { mapOf("email" to it.user, "password" to it.pw) }))
            .response()
        return response.headers["Set-Cookie"].first()
    }
}

private fun Response.html(): String {
    return body().asString(headers[Headers.CONTENT_TYPE].lastOrNull())

}
