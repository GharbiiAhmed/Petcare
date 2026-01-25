package tn.petcare_android.data.api

import retrofit2.http.*
import tn.petcare_android.data.model.medical.MedicalHistoryRequest
import tn.petcare_android.data.model.medical.MedicalHistoryResponse

/**
 * Medical History API endpoints
 * iOS Reference: Pet.swift MedicalHistory
 * Backend endpoints for pet medical records
 */
interface MedicalHistoryApi {
    
    /**
     * Get medical history for a specific pet
     * iOS Reference: APIClient.swift fetchPet (includes medicalHistory)
     */
    @GET("pets/{petId}/medical-history")
    suspend fun getMedicalHistory(
        @Path("petId") petId: String
    ): MedicalHistoryResponse
    
    /**
     * Update pet's medical history (vaccinations, conditions, medications)
     * iOS Reference: APIClient.swift updatePet
     */
    @PUT("pets/{petId}/medical-history")
    suspend fun updateMedicalHistory(
        @Path("petId") petId: String,
        @Body request: MedicalHistoryRequest
    ): MedicalHistoryResponse
    
    /**
     * Add vaccination to pet's record
     */
    @POST("pets/{petId}/medical-history/vaccinations")
    suspend fun addVaccination(
        @Path("petId") petId: String,
        @Body vaccination: Map<String, String>
    ): MedicalHistoryResponse
    
    /**
     * Add chronic condition to pet's record
     */
    @POST("pets/{petId}/medical-history/conditions")
    suspend fun addCondition(
        @Path("petId") petId: String,
        @Body condition: Map<String, String>
    ): MedicalHistoryResponse
    
    /**
     * Add medication to pet's record
     */
    @POST("pets/{petId}/medical-history/medications")
    suspend fun addMedication(
        @Path("petId") petId: String,
        @Body medication: Map<String, String>
    ): MedicalHistoryResponse
    
    /**
     * Remove vaccination from pet's record
     */
    @DELETE("pets/{petId}/medical-history/vaccinations/{vaccination}")
    suspend fun removeVaccination(
        @Path("petId") petId: String,
        @Path("vaccination") vaccination: String
    ): MedicalHistoryResponse
    
    /**
     * Remove chronic condition from pet's record
     */
    @DELETE("pets/{petId}/medical-history/conditions/{condition}")
    suspend fun removeCondition(
        @Path("petId") petId: String,
        @Path("condition") condition: String
    ): MedicalHistoryResponse
    
    /**
     * Remove medication from pet's record
     */
    @DELETE("pets/{petId}/medical-history/medications/{medication}")
    suspend fun removeMedication(
        @Path("petId") petId: String,
        @Path("medication") medication: String
    ): MedicalHistoryResponse
}

