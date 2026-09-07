package com.example.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    suspend fun signInAnonymously(): String {
        return try {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            user?.uid ?: throw Exception("Auth failed")
        } catch (e: Exception) {
            Log.e("Firebase", "Auth Error", e)
            throw e
        }
    }

    fun getUserFlow(uid: String): Flow<User> = callbackFlow {
        val listener = usersRef.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    trySend(user)
                }
            } else {
                // Create default user
                val newUser = User(uid = uid)
                usersRef.document(uid).set(newUser)
                trySend(newUser)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getTransactionsFlow(uid: String): Flow<List<Transaction>> = callbackFlow {
        val listener = usersRef.document(uid).collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    fun addCoins(uid: String, amount: Int, reason: String) {
        db.runTransaction { transaction ->
            val docRef = usersRef.document(uid)
            val snapshot = transaction.get(docRef)
            val currentCoins = snapshot.getLong("coinBalance") ?: 0
            val lifetime = snapshot.getLong("lifetimeEarnings") ?: 0
            
            transaction.update(docRef, "coinBalance", currentCoins + amount)
            transaction.update(docRef, "lifetimeEarnings", lifetime + amount)
            
            // Add transaction record
            val txRef = docRef.collection("transactions").document()
            val tx = Transaction(
                id = txRef.id,
                title = reason,
                amount = amount,
                isCredit = true,
                status = "Completed",
                timestamp = System.currentTimeMillis()
            )
            transaction.set(txRef, tx)
        }
    }
    
    fun performCheckIn(uid: String) {
        db.runTransaction { transaction ->
            val docRef = usersRef.document(uid)
            val snapshot = transaction.get(docRef)
            val currentCoins = snapshot.getLong("coinBalance") ?: 0
            val lifetime = snapshot.getLong("lifetimeEarnings") ?: 0
            val canCheckIn = snapshot.getBoolean("canCheckIn") ?: false
            
            if (canCheckIn) {
                transaction.update(docRef, "coinBalance", currentCoins + 10)
                transaction.update(docRef, "lifetimeEarnings", lifetime + 10)
                transaction.update(docRef, "canCheckIn", false)
                
                val txRef = docRef.collection("transactions").document()
                val tx = Transaction(
                    id = txRef.id,
                    title = "Daily Check-in",
                    amount = 10,
                    isCredit = true,
                    status = "Completed",
                    timestamp = System.currentTimeMillis()
                )
                transaction.set(txRef, tx)
            }
        }
    }

    fun updateProfile(uid: String, name: String, paymentId: String) {
        usersRef.document(uid).update(
            "displayName", name,
            "paymentId", paymentId
        )
    }
    
    fun updateCountry(uid: String, country: Country) {
        usersRef.document(uid).update("country", country.name)
    }
    
    fun useMathAttempt(uid: String): Boolean {
        var success = false
        db.runTransaction { transaction ->
            val docRef = usersRef.document(uid)
            val snapshot = transaction.get(docRef)
            val limits = snapshot.getLong("dailyMathLimit") ?: 0
            if (limits > 0) {
                transaction.update(docRef, "dailyMathLimit", limits - 1)
                success = true
            }
        }
        return success
    }
    
    fun useCaptchaAttempt(uid: String): Boolean {
        var success = false
        db.runTransaction { transaction ->
            val docRef = usersRef.document(uid)
            val snapshot = transaction.get(docRef)
            val limits = snapshot.getLong("dailyCaptchaLimit") ?: 0
            if (limits > 0) {
                transaction.update(docRef, "dailyCaptchaLimit", limits - 1)
                success = true
            }
        }
        return success
    }
}
