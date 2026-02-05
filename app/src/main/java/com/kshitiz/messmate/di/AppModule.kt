package com.kshitiz.messmate.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kshitiz.messmate.data.repository.*
import com.kshitiz.messmate.domain.repository.*
import com.kshitiz.messmate.domain.usecase.*
import com.kshitiz.messmate.ui.viewmodel.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Provide Firebase instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
    single<MenuRepository> { MenuRepositoryImpl(get()) }
    single<AttendanceRepository> { AttendanceRepositoryImpl(get()) }
    single<FeedbackRepository> { FeedbackRepositoryImpl(get()) }

    // Use Cases
    factory { LoginUseCase(get()) }
    factory { SignupUseCase(get()) }
    factory { GetProfileUseCase(get()) }
    factory { SaveProfileUseCase(get()) }
    factory { GetDailyMenuUseCase(get()) }
    factory { OptOutMealUseCase(get()) }
    factory { MarkAttendanceUseCase(get()) }
    factory { SubmitFeedbackUseCase(get()) }
    factory { GetFeedbackSummaryUseCase(get()) }
    factory { GetUserAttendanceUseCase(get()) }
    factory { IsAdminUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }

    // ViewModels
    viewModel { AuthViewModel(get(), get(), get(), get(), get()) }
    viewModel { AdminViewModel(get()) }
    viewModel { MenuViewModel(get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { FeedbackViewModel(get(), get()) }
    viewModel { AdminFeedbackViewModel(get()) }
    viewModel { AdminMenuViewModel(get()) }
}