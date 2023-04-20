package ru.vihnin.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import ru.vihnin.services.AccountManager
import ru.vihnin.services.StockApiImpl
import java.lang.NumberFormatException

val accountManager = AccountManager(StockApiImpl("http://localhost:8080"))

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/signUp") {
            val id = call.getInt("id")

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "No id in args")
            } else {
                val result = accountManager.signUp(id)
                if (result) {
                    call.respond("Success")
                } else {
                    call.respondText("Already existing user")
                }
            }
        }

        post("/addMoney") {
            val id = call.getInt("id")
            val money = call.getDouble("money")

            if (id == null || money == null) {
                call.respond(HttpStatusCode.BadRequest, "No id or money in args")
            } else {
                val result = accountManager.addMoney(id, money)
                if (result) {
                    call.respond("Success")
                } else {
                    call.respondText("Fail")
                }
            }
        }

        get("/getStocks") {
            val id = call.getInt("id")

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "No id in args")
            } else {
                val result = accountManager.getStocks(id)
                if (result != null) {
                    call.respond(result)
                } else {
                    call.respondText("Fail")
                }
            }
        }

        get("/getTotal") {
            val id = call.getInt("id")

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "No id in args")
            } else {
                val result = accountManager.getTotalAmount(id)
                if (result != null) {
                    call.respond("Total = $result")
                } else {
                    call.respondText("Fail")
                }
            }
        }

        post("/buyStock") {
            val id = call.getInt("id")
            val cname = call.parameters["campaignName"]

            if (id == null || cname == null) {
                call.respond(HttpStatusCode.BadRequest, "No id or cname in args")
            } else {
                val result = accountManager.buyStock(id, cname)
                if (result) {
                    call.respond("Success")
                } else {
                    call.respondText("Fail")
                }
            }
        }

        post("/cellStock") {
            val id = call.getInt("id")
            val sid = call.getInt("stockId")

            if (id == null || sid == null) {
                call.respond(HttpStatusCode.BadRequest, "No id or cid in args")
            } else {
                val result = accountManager.cellStock(id, sid)
                if (result) {
                    call.respond("Success")
                } else {
                    call.respondText("Fail")
                }
            }
        }
    }
}

private fun ApplicationCall.getInt(name: String): Int? {
    val p = parameters[name] ?: return null
    return try {
        Integer.parseInt(p)
    } catch (e: NumberFormatException) {
        null
    }
}

private fun ApplicationCall.getDouble(name: String): Double? {
    val p = parameters[name] ?: return null
    return try {
        p.toDouble()
    } catch (e: NumberFormatException) {
        null
    }
}
