package com.kshitiz.messmate.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kshitiz.messmate.ui.auth.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Provide Firebase instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    // Provide AuthViewModel with both Auth and Firestore
    viewModel { AuthViewModel(get<FirebaseAuth>(), get<FirebaseFirestore>()) }
}