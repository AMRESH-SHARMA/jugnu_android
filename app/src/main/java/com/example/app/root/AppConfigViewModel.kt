package com.example.app.root


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.BuildConfig
import com.example.app.core.network.ApiResult
import com.example.app.core.network.appconfig.AppConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class AppConfigViewModel @Inject constructor(
    private val repo: AppConfigRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _appConfig = MutableStateFlow(AppConfigState(isLoading = true))
    val appConfig: StateFlow<AppConfigState> = _appConfig

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            // Check network connectivity first
            if (!isNetworkAvailable()) {
                _appConfig.value = AppConfigState(
                    isLoading = false,
                    errorType = ErrorType.NO_INTERNET
                )
                return@launch
            }

            when (val result = repo.fetchConfig()) {

                is ApiResult.Success -> {
                    val cfg = result.data

                    val forceUpdate =
                        BuildConfig.VERSION_CODE < cfg.min_supported_version

                    _appConfig.value = AppConfigState(
                        isLoading = false,
                        forceUpdate = forceUpdate
                    )
                }

                is ApiResult.Error -> {
                    // Determine error type based on exception
                    val errorType = when (result.exception) {
                        is UnknownHostException,
                        is SocketTimeoutException -> ErrorType.SERVER_UNREACHABLE
                        else -> ErrorType.SERVER_UNREACHABLE
                    }

                    _appConfig.value = AppConfigState(
                        isLoading = false,
                        errorType = errorType
                    )
                }
            }
        }
    }

    fun retry() {
        _appConfig.value = AppConfigState(isLoading = true)
        load()
    }

    fun setForceUpdate() {
        _appConfig.value = _appConfig.value.copy(
            isLoading = false,
            forceUpdate = true
        )
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}
