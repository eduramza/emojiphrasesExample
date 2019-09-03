package com.ramattec.rest.api

import com.ramattec.rest.API_VERSION
import com.ramattec.rest.api.requests.PhrasesApiRequest
import com.ramattec.rest.apiUser
import com.ramattec.rest.repository.Repository
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route


const val PHRASE_API_ENDPOINT = "$API_VERSION/phrases"

@Location(PHRASE_API_ENDPOINT)
class PhrasesApi

fun Route.phrasesApi(db: Repository){
    authenticate("jwt") {
        get<PhrasesApi>{
            call.respond(db.phrases())
        }

        post<PhrasesApi>{
            val user = call.apiUser!!

            try {
                val request  = call.receive<PhrasesApiRequest>()
                val phrase = db.add(user.userId, request.emoji, request.phrase)
                if (phrase != null){
                    call.respond(phrase)
                } else {
                    call.respondText("Invalid data reeived", status = HttpStatusCode.InternalServerError)
                }
            } catch (e: Throwable){
                call.respondText("Invalid data received", status = HttpStatusCode.BadRequest)
            }
        }
    }
}