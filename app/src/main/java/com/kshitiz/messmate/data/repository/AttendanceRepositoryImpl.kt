package com.kshitiz.messmate.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kshitiz.messmate.data.FirestoreConstants
import com.kshitiz.messmate.domain.model.UserAttendance
import com.kshitiz.messmate.domain.repository.AttendanceRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AttendanceRepositoryImpl(
    private val firestore: FirebaseFirestore
) : AttendanceRepository {

    override fun getUserAttendance(email: String, date: String): Flow<Resource<UserAttendance>> = callbackFlow {
        trySend(Resource.Loading)
        val subscription = firestore.collection(FirestoreConstants.COLLECTION_USERS)
            .document(email)
            .collection(FirestoreConstants.SUBCOLLECTION_ATTENDANCE)
            .document(date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching attendance"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val attendance = UserAttendance(
                        date = date,
                        breakfast = snapshot.getBoolean("breakfast") ?: false,
                        lunch = snapshot.getBoolean("lunch") ?: false,
                        snacks = snapshot.getBoolean("snacks") ?: false,
                        dinner = snapshot.getBoolean("dinner") ?: false,
                        breakfastOptOut = snapshot.getBoolean("breakfast_optout") ?: false,
                        lunchOptOut = snapshot.getBoolean("lunch_optout") ?: false,
                        snacksOptOut = snapshot.getBoolean("snacks_optout") ?: false,
                        dinnerOptOut = snapshot.getBoolean("dinner_optout") ?: false
                    )
                    trySend(Resource.Success(attendance))
                } else {
                    trySend(Resource.Success(UserAttendance(date = date)))
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun markAttendance(email: String, date: String, mealType: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val mealKey = mealType.lowercase()
            val docRef = firestore.collection(FirestoreConstants.COLLECTION_USERS)
                .document(email)
                .collection(FirestoreConstants.SUBCOLLECTION_ATTENDANCE)
                .document(date)

            val snapshot = docRef.get().await()
            if (snapshot.getBoolean(mealKey) == true) {
                return@withContext Resource.Error("Attendance already marked")
            }
            if (snapshot.getBoolean("${mealKey}_optout") == true) {
                return@withContext Resource.Error("Student has opted out. Entry Denied.")
            }

            docRef.set(mapOf(mealKey to true), SetOptions.merge()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark attendance")
        }
    }

    override suspend fun optOutMeal(email: String, date: String, mealType: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val optOutKey = "${mealType.lowercase()}_optout"
            firestore.collection(FirestoreConstants.COLLECTION_USERS)
                .document(email)
                .collection(FirestoreConstants.SUBCOLLECTION_ATTENDANCE)
                .document(date)
                .set(mapOf(optOutKey to true), SetOptions.merge())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to opt out")
        }
    }
}
