package ru.vihnin.services

import ru.vihnin.Stock
import kotlin.random.Random

data class Company(val name: String, val id: Int)

class StockMarket {
    private val companyIds: MutableSet<Int> = HashSet()
    private val stocksIds: MutableSet<Int> = HashSet()
    private val companyNames: MutableMap<String, Company> = HashMap()
    private val companies: MutableMap<Int, Pair<Company, MutableSet<Stock>>> = HashMap()
    private val stocks: MutableMap<Int, Stock> = HashMap()
    private val companyStocksPrice: MutableMap<Int, Double> = HashMap()
    private val stocksToHolder: MutableMap<Int, Int> = HashMap()
    private val holderToStocks: MutableMap<Int, MutableSet<Int>> = HashMap()


    private fun getNewId(ids: MutableSet<Int>): Int {
        while (true) {
            val newId = Random.nextInt()
            if (!ids.contains(newId)) {
                return newId
            }
        }
    }

    fun getName(id : Int) : String? {
        if (!companies.containsKey(id)) return null

        return companies[id]!!.first.name
    }

    fun addCompany(name: String): Boolean {
        if (companyNames.containsKey(name)) return false

        val newId = getNewId(companyIds)
        companyIds.add(newId)
        val newCompany = Company(name, newId)
        companyNames[name] = newCompany
        companies[newId] = newCompany to HashSet()
        companyStocksPrice[newId] = 0.0

        return true
    }

    fun addStocks(name: String, numberOfStocks: Int): Boolean {
        if (!companyNames.containsKey(name)) return false

        val company = companyNames[name]!!
        val (_, stocks) = companies[company.id]!!

        for (i in 0 until numberOfStocks) {
            val newStockId = getNewId(stocksIds)
            stocks.add(Stock(newStockId, company.id))
            this.stocks[newStockId] = Stock(newStockId, company.id)
        }

        return true
    }

    fun getCountAndPrice(name: String): Pair<Int, Double>? {
        if (!companyNames.containsKey(name)) return null
        val company = companyNames[name]!!
        val stocks = companies[company.id]!!.second

        return stocks.size to companyStocksPrice[company.id]!!
    }

    fun buyStock(companyName: String, holderId: Int): Boolean {
        if (!companyNames.containsKey(companyName)) return false

        val company = companyNames[companyName]!!
        val stocks = companies[company.id]!!.second

        return if (stocks.size > 0) {
            val stock = stocks.drop(1)[0]
            stocksToHolder[stock.id] = holderId
            holderToStocks.getOrPut(holderId) { HashSet() }.add(stock.id)
            true
        } else false
    }

    fun sellStock(stockId: Int, holderId: Int): Boolean {
        if (!stocksToHolder.containsKey(stockId) || stocksToHolder[stockId] != holderId) return false

        val stock = stocks[stockId]!!
        val (_, stocks) = companies[stock.companyId]!!
        holderToStocks[holderId]!!.remove(stockId)

        stocks.add(stock)
        return true
    }

    fun getStockPrice(companyName: String): Double? {
        if (!companyNames.containsKey(companyName)) return null
        return companyStocksPrice.getOrDefault(companyNames[companyName]!!.id, 0.0)
    }

    fun changeStockPrice(companyName: String, newPrice: Double): Boolean {
        if (!companyNames.containsKey(companyName)) return false
        companyStocksPrice[companyNames[companyName]!!.id] = newPrice
        return true
    }

    fun getStocks(holderId: Int): List<Stock> {
        return holderToStocks.getOrDefault(holderId, HashSet())
            .toList()
            .map {
            stocks[it]!!
        }
    }
}


