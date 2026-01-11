package com.example.app.core.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
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

        ApiResult.Error(
            message = backendMessage ?: e.message(),
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
