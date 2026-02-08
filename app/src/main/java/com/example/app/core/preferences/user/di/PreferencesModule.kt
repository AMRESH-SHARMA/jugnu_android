package com.example.app.preferences.user.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.example.app.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideEncryptedDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {

        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val file = File(
            context.filesDir,
            "datastore/${AppConstants.DATASTORE_FILE_NAME}.preferences_pb"
        )
        file.parentFile?.mkdirs()

        EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        return PreferenceDataStoreFactory.create(
            produceFile = { file }
        )
    }
}

//@Module
//@InstallIn(SingletonComponent::class)
//object UserPreferencesModule {
//
//    @Provides
//    @Singleton
//    fun providePreferencesDataStore(
//        @ApplicationContext context: Context
//    ): DataStore<Preferences> {
//        return PreferenceDataStoreFactory.create(
//            produceFile = { context.preferencesDataStoreFile(AppConstants.DATASTORE_FILE_NAME) }
//        )
//    }
//}