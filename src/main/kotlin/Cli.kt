import com.github.ajalt.clikt.core.CliktCommand
import com.sksamuel.hoplite.ConfigLoader

class Cli : CliktCommand() {
    val user: String = System.getenv("USER") ?: throw RuntimeException("USER not provided")
    val pw: String = System.getenv("PW") ?: throw RuntimeException("PW not provided")

    val login = Credentials(user, pw)
    val cfg = ConfigLoader().loadConfigOrThrow<Config>("/application.yaml")

    val sess = Session(login, cfg.server)

    //val count: Int by option(help="Number of greetings").int().default(1)
    //val name: String by option(help="The person to greet").prompt("Your name")

    override fun run() {
        println(InactiveService(sess, cfg).getInactive())
    }
}

fun main(args: Array<String>) = Cli().main(args)


