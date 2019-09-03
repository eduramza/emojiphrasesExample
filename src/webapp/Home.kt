package com.ramattec.rest.webapp

import com.ramattec.rest.model.EPSession
import com.ramattec.rest.repository.Repository
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions

const val HOME = "/"

@Location(HOME)
class Home

fun Route.home(db: Repository){
    get<Home>{
        val user = call.sessions.get<EPSession>()?.let { db.user(it.userId) }
        call.respond(FreeMarkerContent("home.ftl", mapOf("user" to user)))
    }
}