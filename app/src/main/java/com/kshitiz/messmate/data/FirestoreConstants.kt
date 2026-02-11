package com.kshitiz.messmate.data

/**
 * Centralized Firestore collection and document name constants.
 * Keeps all Firestore paths in a single location for easy maintenance.
 */
object FirestoreConstants {
    const val COLLECTION_USERS = "users"
    const val COLLECTION_MENUS = "menus"
    const val COLLECTION_ADMINS = "admins"
    const val COLLECTION_FEEDBACK = "feedback"
    const val SUBCOLLECTION_ATTENDANCE = "attendance"
}
