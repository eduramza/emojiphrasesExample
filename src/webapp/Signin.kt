package com.ramattec.rest.webapp

import com.ramattec.rest.*
import com.ramattec.rest.model.EPSession
import com.ramattec.rest.repository.Repository
import com.ramattec.rest.usernameValid
import io.ktor.application.call
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.Parameters
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set


const val SIGNIN = "/signin"

@Location(SIGNIN)
data class Signin(val userId: String = "", val error: String = "")

fun Route.signin(db: Repository, hashFunction: (String) -> String){

    post<Signin> {
        val signInParameters = call.receive<Parameters>()
        val userId = signInParameters["userId"] ?: return@post call.redirect(it)
        val password = signInParameters["password"] ?: return@post call.redirect(it)

        val signInError = Signin(userId)

        val signin = when {
            userId.length < MIN_USER_ID_LENGTH -> null
            password.length < MIN_PASSWORD_LENGTH -> null
            !usernameValid(userId) -> null
            else -> db.user(userId, hashFunction(password))
        }

        if (signin == null) {
            call.redirect(signInError.copy(error = "Invalid username or password"))
        } else {
            call.sessions.set(EPSession(signin.userId))
            call.redirect(Phrases())
        }

    }

    get<Signin> {
        val user = call.sessions.get<EPSession>()?.let { db.user(it.userId) }

        if (user != null) {
            call.redirect(Home())
        } else {
            call.respond(FreeMarkerContent("signin.ftl", mapOf("userId" to it.userId, "error" to it.error), ""))
        }
    }
}