package com.kshitiz.messmate.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kshitiz.messmate.data.FirestoreConstants
import com.kshitiz.messmate.domain.model.DailyMenu
import com.kshitiz.messmate.domain.repository.MenuRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class MenuRepositoryImpl(
    private val firestore: FirebaseFirestore
) : MenuRepository {

    override fun getDailyMenu(day: String): Flow<Resource<DailyMenu>> = callbackFlow {
        trySend(Resource.Loading)
        
        val dayTitle = day.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val dayLower = day.lowercase(Locale.getDefault())

        val subscription = firestore.collection(FirestoreConstants.COLLECTION_MENUS).document(dayTitle)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching menu"))
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    trySend(Resource.Success(parseMenu(snapshot)))
                } else {
                    // Try lowercase fallback if not already tried or if it's different
                    if (dayTitle != dayLower) {
                        firestore.collection(FirestoreConstants.COLLECTION_MENUS).document(dayLower).get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    trySend(Resource.Success(parseMenu(doc)))
                                } else {
                                    trySend(Resource.Success(DailyMenu()))
                                }
                            }
                            .addOnFailureListener { e ->
                                trySend(Resource.Error(e.message ?: "Error fetching menu fallback"))
                            }
                    } else {
                        trySend(Resource.Success(DailyMenu()))
                    }
                }
            }
        
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateMenu(day: String, menu: DailyMenu): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val dayTitle = day.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val data = mapOf(
                "breakfast" to menu.breakfast,
                "lunch" to menu.lunch,
                "snacks" to menu.snacks,
                "dinner" to menu.dinner
            )
            firestore.collection(FirestoreConstants.COLLECTION_MENUS).document(dayTitle).set(data).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update menu")
        }
    }

    private fun parseMenu(doc: com.google.firebase.firestore.DocumentSnapshot): DailyMenu {
        return DailyMenu(
            breakfast = (doc.get("breakfast") as? List<String>) ?: (doc.get("Breakfast") as? List<String>) ?: emptyList(),
            lunch = (doc.get("lunch") as? List<String>) ?: (doc.get("Lunch") as? List<String>) ?: emptyList(),
            snacks = (doc.get("snacks") as? List<String>) ?: (doc.get("Snacks") as? List<String>) ?: emptyList(),
            dinner = (doc.get("dinner") as? List<String>) ?: (doc.get("Dinner") as? List<String>) ?: emptyList()
        )
    }
}
