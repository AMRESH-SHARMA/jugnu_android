package com.example.app.root


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.BuildConfig
import com.example.app.core.network.ApiResult
import com.example.app.core.network.appconfig.AppConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppConfigViewModel @Inject constructor(
    private val repo: AppConfigRepository
) : ViewModel() {

    private val _appConfig = MutableStateFlow(AppConfigState(isLoading = true))
    val appConfig: StateFlow<AppConfigState> = _appConfig

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
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
                    _appConfig.value = AppConfigState(
                        isLoading = false,
                        forceUpdate = false   // don’t break app when offline
                    )
                }
            }
        }
    }

    fun setForceUpdate() {
        _appConfig.value = _appConfig.value.copy(
            isLoading = false,
            forceUpdate = true
        )
    }

}
