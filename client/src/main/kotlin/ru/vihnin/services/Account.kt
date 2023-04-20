package ru.vihnin.services

import ru.vihnin.Price
import ru.vihnin.Stocks
import kotlin.random.Random

data class Account(val id: Int, var money: Double)

class AccountManager(private val api: StocksApi) {
    private val users: MutableSet<Int> = HashSet()
    private val accounts: MutableMap<Int, Account> = HashMap()

    private fun getNewId(ids: Set<Int>): Int {
        while (true) {
            val newId = Random.nextInt()
            if (!ids.contains(newId)) {
                return newId
            }
        }
    }

    fun signUp(id: Int): Boolean {
        if (users.contains(id)) return false
        users.add(id)
        accounts[id] = Account(id, 0.0)
        return true
    }

    fun addMoney(id: Int, money: Double): Boolean {
        if (!users.contains(id)) return false
        accounts[id]!!.money += money
        return true
    }

    fun getStocks(id: Int) : List<Pair<Price, Stocks>>? {
        if (!users.contains(id)) return null

        return api.getStocks(id)
            .groupBy { it.companyId }
            .toList()
            .map {
                val name = api.getCampaignName(it.first)
                Price(name, api.getStockPrice(name) ?: 0.0) to Stocks(it.second)
            }
    }

    fun getTotalAmount(id: Int) : Double? {
        if (!users.contains(id)) return null

        val prices = getStocks(id)!!
            .map { it.first.price * it.second.stocks.size }

        return accounts[id]!!.money + if (prices.isEmpty()) 0.0 else prices.reduce { acc, d -> acc + d }
    }

    fun buyStock(id: Int, campaignName : String) : Boolean {
        if (!users.contains(id)) return false

        val cost = api.getStockPrice(campaignName) ?: 0.0
        val account = accounts[id]!!

        return if (account.money - cost >= 0) {
            account.money -= cost
            if (!api.buyStock(campaignName, id)) {
                account.money += cost
                false
            } else true
        } else false
    }

    fun cellStock(id: Int, stockId : Int) : Boolean {
        if (!users.contains(id)) return false

        val stocks = api.getStocks(userId = id).filter { it.id == stockId }
        if (stocks.isEmpty()) return false

        val stock = stocks[0]

        return if (api.sellStock(stockId, id)) {
            accounts[id]!!.money += api.getStockPrice(api.getCampaignName(stock.companyId)) ?: 0.0
            true
        } else false
    }

}

