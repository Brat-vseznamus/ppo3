package ru.vihnin

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.vihnin.plugins.*
import ru.vihnin.services.AccountManager
import ru.vihnin.services.StockApiImpl
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Testcontainers
class ApplicationTest {
    private lateinit var host : String
    private lateinit var accountManager : AccountManager

    companion object {
        @Container
        val stockExchange = GenericContainer(DockerImageName.parse("market-docker-image:0.0.1"))
            .withExposedPorts(8080)

        val COMPANY_1 = "com1"
        val COMPANY_2 = "com2"

    }

    fun init() {
        host = "http://localhost:${stockExchange.firstMappedPort}"
        accountManager = AccountManager(StockApiImpl(host))
    }


    @Test
    fun createUserAndAddMoney() {
        basicSetUp()

        val ID = 10

        assert(accountManager.signUp(ID))
        assert(accountManager.addMoney(ID, 100.0))
    }

    @Test
    fun buyStock() {
        basicSetUp()

        val ID = 10

        assert(accountManager.signUp(ID))
        assert(accountManager.addMoney(ID, 100.0))

        assert(accountManager.buyStock(ID, COMPANY_1))

        val stocks = accountManager.getStocks(ID)

        assertNotNull(stocks)
        assert(stocks.size == 1)
    }

    @Test
    fun cellStock() {
        basicSetUp()

        val ID = 10

        assert(accountManager.signUp(ID))
        assert(accountManager.addMoney(ID, 100.0))

        assert(accountManager.buyStock(ID, COMPANY_1))

        val stocks = accountManager.getStocks(ID)

        assertNotNull(stocks)
        assert(stocks.size == 1)

        assert(accountManager.cellStock(ID, stocks[0].second.stocks[0].id))

        val newStocks = accountManager.getStocks(ID)

        assertNotNull(newStocks)
        assert(newStocks.isEmpty())
    }

    @Test
    fun getTotal() {
        basicSetUp()

        val ID = 10

        assert(accountManager.signUp(ID))
        assert(accountManager.addMoney(ID, 100.0))

        assert(accountManager.buyStock(ID, COMPANY_1))

        val total1 = accountManager.getTotalAmount(ID)
        assertNotNull(total1)
        assertEquals(total1, 100.0)

        // change price, except total gets up
        connect {
            val price = 25
            val res: String = post("$host/changePrice?name=$COMPANY_1&price=$price").body()
            assertEquals("Success", res)
        }

        val total2 = accountManager.getTotalAmount(ID)
        assertNotNull(total2)
        assertEquals(total2, 110.0)
    }

    private fun basicSetUp() {
        init()
        testApplication {
            application {
                configureSerialization()
                configureRouting()
            }
        }

        connect {
            val res = post("$host/addCompany?name=$COMPANY_1")
            assertEquals(res.status, HttpStatusCode.OK)
        }

        connect {
            val stocks = 10
            val res: String = post("$host/addStocks?name=$COMPANY_1&count=$stocks").body()
            assertEquals("$stocks stocks added to company \"$COMPANY_1\"", res)
        }

        connect {
            val price = 15
            val res: String = post("$host/changePrice?name=$COMPANY_1&price=$price").body()
            assertEquals("Success", res)
        }
    }

    private fun <T> connect(block: suspend HttpClient.() -> T) : T {
        return runBlocking {
            httpClient().use { client ->
                return@use client.block()
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
}
