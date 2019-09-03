package com.ramattec.rest.api

import com.ramattec.rest.JwtService
import com.ramattec.rest.hash
import com.ramattec.rest.redirect
import com.ramattec.rest.repository.Repository
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route

const val LOGIN_ENDPOINT = "/login"

@Location(LOGIN_ENDPOINT)
class Login

fun Route.login(db: Repository, jwtService: JwtService){

    post<Login>{
        val params = call.receive<Parameters>()
        val userId = params["userId"] ?: return@post call.redirect(it)
        val password = params["password"] ?: return@post call.redirect(it)

        val user = db.user(userId = userId,
            hash = hash(password))

        if (user != null){
            val token = jwtService.generateToken(user)
            call.respondText(token)
        } else {
            call.respondText("Invalid User")
        }
    }
}