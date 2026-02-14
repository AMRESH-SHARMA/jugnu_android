package com.example.app.core.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: kotlinx.coroutines.CancellationException) {
        // Re-throw cancellation to allow proper coroutine cancellation
        throw e
    } catch (e: HttpException) {

        val errorBody = e.response()?.errorBody()?.string()

        val backendMessage = try {
            Gson().fromJson(
                errorBody,
                ApiErrorResponse::class.java
            )?.message
        } catch (ex: Exception) {
            null
        }

        // Use centralized error handler
        val userFriendlyMessage = ApiErrorHandler.getErrorMessage(e, backendMessage)

        ApiResult.Error(
            message = userFriendlyMessage,
            code = e.code(),
            exception = e
        )

    } catch (e: IOException) {
        ApiResult.Error(
            message = ApiErrorHandler.getErrorMessage(e),
            exception = e
        )
    } catch (e: Exception) {
        ApiResult.Error(
            message = ApiErrorHandler.getErrorMessage(e),
            exception = e
        )
    }
}
