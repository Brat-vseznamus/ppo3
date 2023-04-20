package ru.vihnin

import kotlinx.serialization.Serializable


@Serializable
data class Stock(val id: Int, val companyId: Int)

@Serializable
data class Stocks(val stocks: List<Stock>)

@Serializable
data class Price(val companyName: String, val price: Double)
