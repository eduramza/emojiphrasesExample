package com.ramattec.rest

import com.ramattec.rest.api.*
import com.ramattec.rest.model.*
import com.ramattec.rest.repository.*
import com.ramattec.rest.webapp.*
import freemarker.cache.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.request.header
import io.ktor.request.host
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import java.net.URI
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(DefaultHeaders)
    install(StatusPages){
        exception<Throwable> { e ->
            call.respondText (e.localizedMessage,
                ContentType.Text.Plain, HttpStatusCode.InternalServerError)
        }
    }
    install(ContentNegotiation){
        gson()
    }
    install(FreeMarker){
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Locations)
    install(Sessions) {
        cookie<EPSession>("SESSION") {
            transform(SessionTransportTransformerMessageAuthentication(hashKey))
        }
    }

    val hashFunction = {s : String -> hash(s)}
    DatabaseFactory.init()
    val db = EmojiPhrasesRepository()
    val jwtService = JwtService()

    install(Authentication){
        jwt("jwt"){
            verifier(jwtService.verifier)
            realm = "emojiphrases app"
            validate{
                val payload = it.payload
                val claim = payload.getClaim("id")
                val claimString = claim.asString()
                val user = db.userById(claimString)
                user

            }
        }
    }

    routing{
        static("/static") {
            resources("images")
        }

        home(db)
        about(db)
        phrases(db, hashFunction)
        signin(db, hashFunction)
        signout()
        signup(db, hashFunction)

        //API
        login(db, jwtService)
        phrasesApi(db)
    }
}

const val API_VERSION = "/api/v1"

suspend fun ApplicationCall.redirect(location: Any){
    respondRedirect(application.locations.href(location))
}

fun ApplicationCall.verifyCode(date: Long, user: User, code: String, hashFunction: (String) -> String) =
    securityCode(date, user, hashFunction) == code
            && (System.currentTimeMillis() - date).let { it > 0 && it < TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS) }

fun ApplicationCall.securityCode(date: Long, user: User, hashFunction: (String) -> String) =
    hashFunction("$date:${user.userId}:${request.host()}:${refererHost()}")

fun ApplicationCall.refererHost() = request.header(HttpHeaders.Referrer)?.let { URI.create(it).host }

val ApplicationCall.apiUser get() = authentication.principal<User>()