package com.example.passwordmanager.di

import android.content.Context
import com.example.passwordmanager.database.PasswordDao
import com.example.passwordmanager.database.PasswordDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun providePasswordDatabase(@ApplicationContext context: Context): PasswordDatabase {
        return PasswordDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun providePasswordDao(passwordDatabase: PasswordDatabase): PasswordDao {
        return passwordDatabase.passwordDao()
    }

}