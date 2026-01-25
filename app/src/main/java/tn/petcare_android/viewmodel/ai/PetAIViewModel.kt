package tn.petcare_android.viewmodel.ai

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.ai.*
import tn.petcare_android.data.storage.TokenManager
import java.util.*

class PetAIViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val aiApi = RetrofitInstance.aiApi

    // Per-pet data for HomeScreen
    private val _petTips = MutableStateFlow<Map<String, List<PetTip>>>(emptyMap())
    val petTips: StateFlow<Map<String, List<PetTip>>> = _petTips.asStateFlow()

    private val _petStatuses = MutableStateFlow<Map<String, PetStatus>>(emptyMap())
    val petStatuses: StateFlow<Map<String, PetStatus>> = _petStatuses.asStateFlow()

    private val _petReminders = MutableStateFlow<Map<String, List<PetReminder>>>(emptyMap())
    val petReminders: StateFlow<Map<String, List<PetReminder>>> = _petReminders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Cache with timestamps (24 hour TTL)
    private val tipsCache = mutableMapOf<String, Pair<List<PetTip>, Long>>()
    private val statusCache = mutableMapOf<String, Pair<PetStatus, Long>>()
    private val remindersCache = mutableMapOf<String, Pair<List<PetReminder>, Long>>()
    private val cacheTTL = 24 * 60 * 60 * 1000L // 24 hours in milliseconds

    /**
     * Generate tips for a pet
     */
    fun generateTips(petId: String) {
        viewModelScope.launch {
            // Check cache first (outside try block for error handling)
            val cached = tipsCache[petId]
            if (cached != null && System.currentTimeMillis() - cached.second < cacheTTL) {
                Log.d("PetAIViewModel", "Using cached tips for pet $petId")
                updatePetTips(petId, cached.first)
                // Still try to refresh in background
            }
            
            try {
                _isLoading.value = true
                _error.value = null

                val response = aiApi.getTips(petId)
                
                val tips = response.tips.map { tip ->
                    PetTip(
                        id = UUID.randomUUID().toString(),
                        emoji = tip.emoji,
                        title = tip.title,
                        detail = tip.detail
                    )
                }

                // Update cache
                tipsCache[petId] = Pair(tips, System.currentTimeMillis())
                updatePetTips(petId, tips)

                Log.d("PetAIViewModel", "Generated ${tips.size} tips for pet $petId")
                    
            } catch (e: Exception) {
                Log.e("PetAIViewModel", "Error generating tips for pet $petId", e)
                // Show cached content even if new generation fails (iOS Reference: HomeView.swift lines 546-550)
                if (cached != null) {
                    Log.d("PetAIViewModel", "Using cached tips due to error")
                    updatePetTips(petId, cached.first)
                } else {
                    _error.value = "Failed to generate tips: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Generate status for a pet
     */
    fun generateStatus(petId: String) {
        viewModelScope.launch {
            try {
                // Check cache first
                val cached = statusCache[petId]
                if (cached != null && System.currentTimeMillis() - cached.second < cacheTTL) {
                    Log.d("PetAIViewModel", "Using cached status for pet $petId")
                    updatePetStatus(petId, cached.first)
                    return@launch
                }

                _isLoading.value = true
                _error.value = null

                val response = aiApi.getStatus(petId)
                
                val status = PetStatus(
                    status = response.status,
                    summary = response.summary,
                    pills = response.pills.map { pill ->
                        StatusPill(
                            text = pill.text,
                            backgroundColor = parseColor(pill.bg),
                            textColor = parseColor(pill.fg)
                        )
                    }
                )

                // Update cache
                statusCache[petId] = Pair(status, System.currentTimeMillis())
                updatePetStatus(petId, status)

                Log.d("PetAIViewModel", "Generated status for pet $petId: ${status.status}")
                
            } catch (e: Exception) {
                Log.e("PetAIViewModel", "Error generating status for pet $petId", e)
                // Use cached content if available (iOS Reference: HomeView.swift lines 546-550)
                val cached = statusCache[petId]
                if (cached != null) {
                    Log.d("PetAIViewModel", "Using cached status due to error")
                    updatePetStatus(petId, cached.first)
                } else {
                    _error.value = "Failed to generate status: ${e.message}"
                    // Fallback to default status
                    val fallback = PetStatus(
                        status = "Healthy",
                        summary = "All good",
                        pills = emptyList()
                    )
                    updatePetStatus(petId, fallback)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Generate reminders for a pet
     */
    fun generateReminders(petId: String) {
        viewModelScope.launch {
            try {
                // Check cache first
                val cached = remindersCache[petId]
                if (cached != null && System.currentTimeMillis() - cached.second < cacheTTL) {
                    Log.d("PetAIViewModel", "Using cached reminders for pet $petId")
                    updatePetReminders(petId, cached.first)
                    return@launch
                }

                _isLoading.value = true
                _error.value = null

                val response = aiApi.getReminders(petId)
                
                val reminders = response.reminders.map { reminder ->
                    PetReminder(
                        id = UUID.randomUUID().toString(),
                        icon = reminder.icon,
                        title = reminder.title,
                        detail = reminder.detail,
                        date = reminder.date,
                        tint = parseColor(reminder.tint)
                    )
                }

                // Update cache
                remindersCache[petId] = Pair(reminders, System.currentTimeMillis())
                updatePetReminders(petId, reminders)

                Log.d("PetAIViewModel", "Generated ${reminders.size} reminders for pet $petId")
                
            } catch (e: Exception) {
                Log.e("PetAIViewModel", "Error generating reminders for pet $petId", e)
                _error.value = "Failed to generate reminders: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Generate all AI content for a pet (tips, status, reminders)
     */
    fun generateAllContent(petId: String, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _isLoading.value = true
            }
            
            try {
                generateTips(petId)
                generateStatus(petId)
                generateReminders(petId)
            } catch (e: Exception) {
                Log.e("PetAIViewModel", "Error generating all content for pet $petId", e)
                if (!silent) {
                    _error.value = "Failed to generate AI content: ${e.message}"
                }
            } finally {
                if (!silent) {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Generate content for multiple pets with progressive loading (per-pet)
     * iOS Reference: HomeView.swift lines 437-503
     * Updates UI immediately after each pet's content is generated
     */
    fun generateContentForPets(petIds: List<String>, silent: Boolean = true) {
        viewModelScope.launch {
            // Process each pet one by one for progressive loading
            petIds.forEachIndexed { index, petId ->
                try {
                    // Generate tips first (updates UI immediately)
                    generateTips(petId)
                    
                    // Generate status (updates UI immediately)
                    generateStatus(petId)
                    
                    // Generate reminders (updates UI immediately)
                    generateReminders(petId)
                    
                    Log.d("PetAIViewModel", "✅ Processed pet ${index + 1}/${petIds.size}: $petId")
                } catch (e: Exception) {
                    Log.e("PetAIViewModel", "⚠️ Failed to process pet $petId", e)
                    // Continue with next pet even if this one fails
                }
            }
        }
    }

    /**
     * Clear cache for a specific pet
     */
    fun clearCache(petId: String) {
        tipsCache.remove(petId)
        statusCache.remove(petId)
        remindersCache.remove(petId)
    }

    /**
     * Clear all cache
     */
    fun clearAllCache() {
        tipsCache.clear()
        statusCache.clear()
        remindersCache.clear()
    }

    // Helper methods to update state
    private fun updatePetTips(petId: String, tips: List<PetTip>) {
        _petTips.value = _petTips.value.toMutableMap().apply {
            this[petId] = tips
        }
    }

    private fun updatePetStatus(petId: String, status: PetStatus) {
        _petStatuses.value = _petStatuses.value.toMutableMap().apply {
            this[petId] = status
        }
    }

    private fun updatePetReminders(petId: String, reminders: List<PetReminder>) {
        _petReminders.value = _petReminders.value.toMutableMap().apply {
            this[petId] = reminders
        }
    }

    // Parse color from hex string
    private fun parseColor(colorString: String): Color {
        return try {
            val cleanColor = colorString.removePrefix("#")
            val colorInt = cleanColor.toLong(16)
            Color(colorInt)
        } catch (e: Exception) {
            Log.e("PetAIViewModel", "Error parsing color: $colorString", e)
            Color.Gray
        }
    }
}
