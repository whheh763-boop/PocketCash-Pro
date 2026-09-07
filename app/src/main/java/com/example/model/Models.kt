package com.example.model

enum class Country(val displayName: String, val currencySymbol: String, val exchangeRatePer1000: Double) {
    INDIA("India", "₹", 10.0),
    NEPAL("Nepal", "NRs", 16.0)
}

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "Guest User",
    val paymentId: String = "",
    val deviceId: String = "",
    val coinBalance: Int = 0,
    val lifetimeEarnings: Int = 0,
    val country: Country = Country.INDIA,
    val referralCode: String = "",
    val referredBy: String = "",
    val dailyMathLimit: Int = 15,
    val dailyCaptchaLimit: Int = 20,
    val canCheckIn: Boolean = true
)

data class Transaction(
    val id: String = "",
    val title: String = "",
    val amount: Int = 0,
    val isCredit: Boolean = true, // true for earning, false for withdrawal
    val status: String = "", // "Completed", "Pending", "Failed"
    val timestamp: Long = 0L
)
