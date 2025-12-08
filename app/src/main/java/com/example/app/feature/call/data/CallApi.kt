package com.example.app.feature.call.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface CallApi {

    @POST("api/v1/call/start")
    suspend fun startCall(
        @Body req: StartCallRequest
    ): BaseResponse<StartCallDto>

    @POST("api/v1/call/accept")
    suspend fun acceptCall(
        @Body req: AcceptCallRequest
    ): BaseResponse<AcceptCallDto>

    @POST("api/v1/call/reject")
    suspend fun rejectCall(
        @Body req: RejectCallRequest
    ): BaseResponse<RejectCallDto>
}



