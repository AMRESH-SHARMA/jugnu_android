package com.example.app.feature.call.data

import com.example.app.core.network.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface CallApi {

    // ---------------- CALL LIFECYCLE ----------------
    @POST("call/start")
    suspend fun startCall(
        @Body req: StartCallRequest
    ): BaseResponse<StartCallDto>

    @POST("call/accept")
    suspend fun acceptCall(
        @Body req: AcceptCallRequest
    ): BaseResponse<AcceptCallDto>

    @POST("call/reject")
    suspend fun rejectCall(
        @Body req: RejectCallRequest
    ): BaseResponse<RejectCallDto>

    @POST("call/end")
    suspend fun endCall(
        @Body req: EndCallRequest
    ): BaseResponse<Unit>
}
