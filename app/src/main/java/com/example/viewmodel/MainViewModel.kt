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
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job

class MainViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private var currentUid: String = ""
    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState.asStateFlow()
    private val _webViewUrl = MutableStateFlow("")
    val webViewUrl: StateFlow<String> = _webViewUrl.asStateFlow()
    private val _webViewTitle = MutableStateFlow("")
    val webViewTitle: StateFlow<String> = _webViewTitle.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    private val _leaderboard = MutableStateFlow<List<User>>(emptyList())
    val leaderboard: StateFlow<List<User>> = _leaderboard.asStateFlow()
    
    private var userFlowJob: Job? = null
    private var txFlowJob: Job? = null
    private var leaderFlowJob: Job? = null

    init {
        // Automatically check if logged in
        val uid = repository.isUserLoggedIn()
        if (uid != null) {
            startObserving(uid)
        }
    }
    
    suspend fun login(email: String, pass: String) {
        val uid = repository.signInWithEmail(email, pass)
        startObserving(uid)
    }
    
    suspend fun signup(email: String, pass: String, country: Country, refCode: String) {
        val uid = repository.signUpWithEmail(email, pass, country, refCode)
        startObserving(uid)
    }
    
    fun logout() {
        repository.logout()
        currentUid = ""
        userFlowJob?.cancel()
        txFlowJob?.cancel()
        _userState.value = User()
        _transactions.value = emptyList()
    }

    private fun startObserving(uid: String) {
        currentUid = uid
        userFlowJob?.cancel()
        txFlowJob?.cancel()
        leaderFlowJob?.cancel()
        
        userFlowJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getUserFlow(currentUid).collect { user ->
                _userState.value = user
            }
        }
        
        txFlowJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getTransactionsFlow(currentUid).collect { txList ->
                _transactions.value = txList
            }
        }
        
        leaderFlowJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getLeaderboardFlow().collect { list ->
                _leaderboard.value = list
            }
        }
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

    fun updateCountry(country: Country) {
        if (currentUid.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateCountry(currentUid, country)
            }
        }
    }

    fun addCoins(amount: Int, reason: String = "Task Reward") {
        if (currentUid.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.addCoins(currentUid, amount, reason)
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

    fun useMathQuizAttempt(onSuccess: () -> Unit, onFail: () -> Unit) {
        if (currentUid.isEmpty() || _userState.value.dailyMathLimit <= 0) {
            onFail()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.useMathAttempt(currentUid)
            withContext(Dispatchers.Main) {
                if (success) {
                    onSuccess()
                } else {
                    onFail()
                }
            }
        }
    }

    fun useCaptchaAttempt(onSuccess: () -> Unit, onFail: () -> Unit) {
        if (currentUid.isEmpty() || _userState.value.dailyCaptchaLimit <= 0) {
            onFail()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.useCaptchaAttempt(currentUid)
            withContext(Dispatchers.Main) {
                if (success) {
                    onSuccess()
                } else {
                    onFail()
                }
            }
        }
    }
}
