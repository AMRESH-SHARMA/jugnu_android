package com.example.app.core.network

import retrofit2.HttpException
import java.io.IOException


suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: HttpException) {
        ApiResult.Error(
            message = e.message(),
            code = e.code(),
            exception = e
        )
    } catch (e: IOException) {
        ApiResult.Error(
            message = "Network error",
            exception = e
        )
    } catch (e: Exception) {
        ApiResult.Error(
            message = e.localizedMessage,
            exception = e
        )
    }
}
