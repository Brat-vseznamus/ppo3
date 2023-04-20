package ru.vihnin.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import ru.vihnin.market
import ru.vihnin.*
import java.lang.NumberFormatException

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/addCompany") {
            var name = call.parameters["name"]
            if (name == null) {
                call.respond(HttpStatusCode.BadRequest, "No name in args")
            } else {
                val result = market.addCompany(name)
                if (result) {
                    call.respondText("Company added")
                } else {
                    call.respondText("Company already exists")
                }
            }
        }

        get("/getName") {
            val id = call.getInt("id")
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "No id in args")
            } else {
                val result = market.getName(id)
                if (result != null) {
                    call.respondText(result)
                } else {
                    call.respondText("No such id")
                }
            }
        }

        post("/addStocks") {
            val name = call.parameters["name"]
            val numberOfStocks = call.getInt("count")

            if (name == null || numberOfStocks == null) {
                call.respond(HttpStatusCode.BadRequest, "No name of company or number of stocks (count) in args")
            } else {
                val result = market.addStocks(name, numberOfStocks)
                if (result) {
                    call.respondText("$numberOfStocks stocks added to company \"${name}\"")
                } else {
                    call.respondText("Company doesn't exist exists")
                }
            }
        }

        get("/getPrice") {
            val name = call.parameters["name"]

            if (name == null) {
                call.respond(HttpStatusCode.BadRequest, "No name of company in args")
            } else {
                val result = market.getStockPrice(name)
                if (result != null) {
                    call.respond(Price(name, result))
                } else {
                    call.respondText("Company doesn't exist")
                }
            }
        }

        get("/getCountAndPrice") {
            val name = call.parameters["name"]

            if (name == null) {
                call.respond(HttpStatusCode.BadRequest, "No name of company in args")
            } else {
                val result = market.getCountAndPrice(name)
                if (result != null) {
                    call.respond(PriceWithNumber(name, result.second, result.first))
                } else {
                    call.respondText("Company doesn't exist")
                }
            }
        }

        post("/buyStocks") {
            val name = call.parameters["name"]
            val holderId = call.getInt("userId")

            if (name == null || holderId == null) {
                call.respond(HttpStatusCode.BadRequest, "No name of company or user id in args")
            } else {
                val result = market.buyStock(name, holderId)
                if (result) {
                    call.respondText("Success")
                } else {
                    call.respondText("Company doesn't exist or doesn't have stocks")
                }
            }
        }

        post("/sellStocks") {
            val stockId = call.getInt("stockId")
            val holderId = call.getInt("userId")

            if (stockId == null || holderId == null) {
                call.respond(HttpStatusCode.BadRequest, "No if of stock or user id in args")
            } else {
                val result = market.sellStock(stockId, holderId)
                if (result) {
                    call.respondText("Success")
                } else {
                    call.respondText("Company doesn't exist")
                }
            }
        }

        get("/getStocks") {
            val holderId = call.getInt("userId")

            if (holderId == null) {
                call.respond(HttpStatusCode.BadRequest, "No id of user in args")
            } else {
                val result = market.getStocks(holderId)
                call.respond(Stocks(result))
            }
        }

        post("/changePrice") {
            val name = call.parameters["name"]
            val newPrice = call.getDouble("price")

            if (name == null || newPrice == null) {
                call.respond(HttpStatusCode.BadRequest, "No name of company or new price in args")
            } else {
                val result = market.changeStockPrice(name, newPrice)
                if (result) {
                    call.respondText("Success")
                } else {
                    call.respondText("Company doesn't exist or doesn't have stocks")
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
