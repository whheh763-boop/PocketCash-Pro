package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Country
import com.example.model.User
import com.example.model.Transaction
import com.example.model.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private var currentUid: String = ""

    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _webViewUrl = MutableStateFlow("")
    val webViewUrl: StateFlow<String> = _webViewUrl.asStateFlow()

    private val _webViewTitle = MutableStateFlow("")
    val webViewTitle: StateFlow<String> = _webViewTitle.asStateFlow()
    
    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                currentUid = repository.signInAnonymously()
                
                // Observe User Flow
                launch {
                    repository.getUserFlow(currentUid).collect { user ->
                        _userState.value = user
                    }
                }
                
                // Observe Transactions Flow
                launch {
                    repository.getTransactionsFlow(currentUid).collect { txList ->
                        _transactions.value = txList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setWebViewContent(title: String, url: String) {
        _webViewTitle.value = title
        _webViewUrl.value = url
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun updateProfile(name: String, paymentId: String) {
        if (currentUid.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateProfile(currentUid, name, paymentId)
            }
        }
    }

    fun fetchTransactions() {
        // Now automatically handled by flow
    }

    fun updateCountry(country: Country) {
        if (currentUid.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateCountry(currentUid, country)
            }
        }
    }

    fun addCoins(amount: Int) {
        if (currentUid.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.addCoins(currentUid, amount, "Task Reward")
            }
        }
    }
    
    fun performDailyCheckIn() {
        if (currentUid.isNotEmpty() && _userState.value.canCheckIn) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.performCheckIn(currentUid)
            }
        }
    }

    fun useMathQuizAttempt(): Boolean {
        if (currentUid.isEmpty() || _userState.value.dailyMathLimit <= 0) return false
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.useMathAttempt(currentUid)
            if (success) {
                repository.addCoins(currentUid, 10, "Math Quiz Reward")
            }
        }
        return true
    }

    fun useCaptchaAttempt(): Boolean {
        if (currentUid.isEmpty() || _userState.value.dailyCaptchaLimit <= 0) return false
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.useCaptchaAttempt(currentUid)
            if (success) {
                repository.addCoins(currentUid, 5, "Captcha Reward")
            }
        }
        return true
    }
}
