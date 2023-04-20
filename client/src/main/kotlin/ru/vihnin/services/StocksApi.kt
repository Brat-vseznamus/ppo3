package ru.vihnin.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.vihnin.Price
import ru.vihnin.Stock
import ru.vihnin.Stocks


interface StocksApi {
    fun getCampaignName(campaignId: Int) : String
    fun getStocks(userId: Int): List<Stock>
    fun getStockPrice(campaignName: String): Double?
    fun buyStock(campaignName: String, userId: Int): Boolean
    fun sellStock(stockId: Int, userId: Int): Boolean
}

class StockApiImpl(private val host : String) : StocksApi {
    override fun getCampaignName(campaignId: Int): String {
        return runBlocking {
            httpClient().use { client ->
                val res : String = client.get("$host/getName?id=$campaignId").body()
                return@use res
            }
        }
    }

    override fun getStocks(userId: Int): List<Stock> {
        return runBlocking {
            httpClient().use { client ->
                val res : Stocks = client.get("$host/getStocks?userId=$userId").body()
                return@use res.stocks
            }
        }
    }

    override fun getStockPrice(campaignName: String): Double {
        return runBlocking {
            httpClient().use { client ->
                val res : Price = client.get("$host/getPrice?name=$campaignName").body()
                return@use res.price
            }
        }
    }

    override fun buyStock(campaignName: String, userId: Int): Boolean {
        return runBlocking {
            httpClient().use { client ->
                val res : String = client.post("$host/buyStocks?name=$campaignName&userId=$userId").body()
                return@use res == "Success"
            }
        }
    }

    private fun httpClient() : HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }

    override fun sellStock(stockId: Int, userId: Int): Boolean {
        return runBlocking {
            httpClient().use { client ->
                val res : String = client.post("$host/sellStocks?stockId=$stockId&userId=$userId").body()

                return@use res == "Success"
            }
        }
    }

}