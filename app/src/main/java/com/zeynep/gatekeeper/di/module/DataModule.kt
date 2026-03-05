package com.zeynep.gatekeeper.di.module

import com.zeynep.gatekeeper.data.repository.BiometricRepositoryImpl
import com.zeynep.gatekeeper.data.source.BiometricSdkWrapper
import com.zeynep.gatekeeper.data.source.DefaultBiometricSdkWrapper
import com.zeynep.gatekeeper.domain.repository.BiometricRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindBiometricSdkWrapper(
        impl: DefaultBiometricSdkWrapper
    ): BiometricSdkWrapper

    @Binds
    @Singleton
    abstract fun bindBiometricRepository(
        impl: BiometricRepositoryImpl
    ): BiometricRepository
}
