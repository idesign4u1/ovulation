package com.ovulation.health.network

import com.ovulation.health.data.model.AdminReport
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for syncing health data with admin server
 */
interface OvulationHealthApiService {

    /**
     * Upload health metrics and reports to server
     */
    @POST("api/v1/health-metrics")
    suspend fun uploadHealthMetrics(@Body report: AdminReport): Response<UploadResponse>

    /**
     * Upload ferning analysis image and data
     */
    @Multipart
    @POST("api/v1/ferning-analysis")
    suspend fun uploadFerningAnalysis(
        @Part("image") image: okhttp3.MultipartBody.Part,
        @Part("data") analysisData: String
    ): Response<UploadResponse>

    /**
     * Upload voice analysis data
     */
    @Multipart
    @POST("api/v1/voice-analysis")
    suspend fun uploadVoiceAnalysis(
        @Part("audio") audio: okhttp3.MultipartBody.Part,
        @Part("metadata") metadata: String
    ): Response<UploadResponse>

    /**
     * Get ovulation prediction chart data
     */
    @GET("api/v1/predictions/ovulation")
    suspend fun getOvulationPredictions(
        @Query("user_id") userId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<PredictionResponse>

    /**
     * Get implantation window data
     */
    @GET("api/v1/predictions/implantation-window")
    suspend fun getImplantationWindows(
        @Query("user_id") userId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<ImplantationWindowResponse>

    /**
     * Send alert notification to admin
     */
    @POST("api/v1/alerts")
    suspend fun sendAlert(@Body alert: AlertRequest): Response<AlertResponse>

    /**
     * Get user settings from server
     */
    @GET("api/v1/users/{user_id}/settings")
    suspend fun getUserSettings(@Path("user_id") userId: String): Response<UserSettings>

    /**
     * Update user settings on server
     */
    @PUT("api/v1/users/{user_id}/settings")
    suspend fun updateUserSettings(
        @Path("user_id") userId: String,
        @Body settings: UserSettings
    ): Response<UserSettings>

    /**
     * Health check endpoint
     */
    @GET("api/v1/health")
    suspend fun healthCheck(): Response<HealthCheckResponse>
}

// Response models
data class UploadResponse(
    val success: Boolean,
    val message: String,
    val dataId: String? = null
)

data class PredictionResponse(
    val userId: String,
    val predictions: List<PredictionData>
)

data class PredictionData(
    val date: String,
    val estimatedOvulation: String,
    val confidence: Float,
    val status: String
)

data class ImplantationWindowResponse(
    val userId: String,
    val windows: List<WindowData>
)

data class WindowData(
    val startDate: String,
    val endDate: String,
    val confidence: Float,
    val status: String
)

data class AlertRequest(
    val userId: String,
    val alertType: String, // OVULATION_PREDICTED, IMPLANTATION_WINDOW, ANOMALY
    val severity: String, // INFO, WARNING, CRITICAL
    val message: String,
    val data: Map<String, Any>? = null
)

data class AlertResponse(
    val success: Boolean,
    val alertId: String,
    val message: String
)

data class UserSettings(
    val userId: String,
    val testTime: String = "08:00", // Default 8 AM
    val cycleLength: Int = 28,
    val notificationsEnabled: Boolean = true,
    val dataRetentionDays: Int = 365,
    val adminEmail: String = "",
    val timezone: String = "UTC"
)

data class HealthCheckResponse(
    val status: String,
    val timestamp: String,
    val version: String
)
