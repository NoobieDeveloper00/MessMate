package com.kshitiz.messmate.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kshitiz.messmate.ui.auth.AuthViewModel
import com.kshitiz.messmate.ui.main.admin.AdminViewModel
import com.kshitiz.messmate.ui.main.menu.MenuViewModel
import com.kshitiz.messmate.ui.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.kshitiz.messmate.ui.main.menu.FeedbackViewModel
import com.kshitiz.messmate.ui.main.admin.feedback.AdminFeedbackViewModel
import com.kshitiz.messmate.ui.main.admin.menu.AdminMenuViewModel

val appModule = module {
    // Provide Firebase instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }

    // Provide AuthViewModel with both Auth and Firestore
    viewModel { AuthViewModel(get<FirebaseAuth>(), get<FirebaseFirestore>()) }
    // Provide AdminViewModel with Firestore
    viewModel { AdminViewModel(get<FirebaseFirestore>()) }
    // Provide MenuViewModel with Auth and Firestore
    viewModel { MenuViewModel(get<FirebaseAuth>(), get<FirebaseFirestore>()) }
    // Provide ProfileViewModel with Auth, Firestore, and Storage
    viewModel { ProfileViewModel(get<FirebaseAuth>(), get<FirebaseFirestore>(), get<FirebaseStorage>()) }

    viewModel { FeedbackViewModel(get<FirebaseAuth>(), get<FirebaseFirestore>()) }

    viewModel { AdminFeedbackViewModel(get<FirebaseFirestore>()) }

    viewModel { AdminMenuViewModel(get()) }
}