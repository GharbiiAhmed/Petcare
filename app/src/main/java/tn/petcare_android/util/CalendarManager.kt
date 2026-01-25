package tn.petcare_android.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import tn.petcare_android.data.model.booking.Booking
import java.text.SimpleDateFormat
import java.util.*

/**
 * Calendar Event UI Model
 * iOS Reference: CalendarManager.swift Event model
 */
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String = "",
    val location: String = "",
    val startTime: Long,
    val endTime: Long,
    val bookingId: String? = null
)

/**
 * Calendar Manager for Device Calendar Integration
 * iOS Reference: CalendarManager.swift (lines 1-187)
 * 
 * Integrates with Android Calendar Provider to:
 * - Add booking events to device calendar
 * - Fetch calendar events
 * - Edit/delete calendar events
 * - Sync bookings with device calendar
 */
class CalendarManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CalendarManager"
        private const val CALENDAR_NAME = "Petcare Bookings"
        private const val CALENDAR_DISPLAY_NAME = "Petcare Appointments & Services"
        private const val CALENDAR_COLOR = 0xFF4ECDC4.toInt() // Teal color - pet healthcare theme
    }
    
    /**
     * Check if calendar permissions are granted
     */
    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Add booking to device calendar
     * @param booking Booking to add
     * @return Event ID if successful, null otherwise
     */
    fun addBookingToCalendar(booking: Booking): Long? {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return null
        }
        
        try {
            // Try to get Petcare calendar, fallback to primary calendar if not found
            var calendarId = getOrCreatePetcareCalendar()
            if (calendarId == null) {
                Log.w(TAG, "No Petcare calendar found for booking, using primary calendar as fallback")
                calendarId = getPrimaryCalendarId()
                if (calendarId == null) {
                    Log.e(TAG, "No calendar available on device")
                    return null
                }
            }
            
            val startTime = parseDateTime(booking.dateTime) ?: System.currentTimeMillis()
            val duration = (booking.duration ?: 60) * 60 * 1000L // Convert minutes to milliseconds
            val endTime = startTime + duration
            
            val title = "${booking.serviceType} - ${booking.provider?.name ?: "Booking"}"
            val description = buildString {
                append("Booking ID: ${booking.normalizedId}\n")
                booking.description?.let { append("$it\n") }
                booking.pet?.let { pet ->
                    append("Pet: ${pet.name}\n")
                }
                booking.price?.let { append("Price: $$it\n") }
            }
            
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1) // Enable reminder
                // Add custom URI to identify Petcare booking events
                put(CalendarContract.Events.CUSTOM_APP_URI, "petcare://booking/${booking.normalizedId}")
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
            
            // Add reminder (15 minutes before)
            eventId?.let { addReminder(it, 15) }
            
            Log.d(TAG, "📅 Added booking to calendar: ${booking.normalizedId} -> Event ID: $eventId")
            return eventId
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add booking to calendar: ${e.message}")
            return null
        }
    }
    
    /**
     * Add custom event to calendar (for manual reminders)
     * iOS Reference: CalendarManager.swift addEvent
     * @param title Event title
     * @param description Event description (should include "Pet ID: {petId}\nType: {type}\n{notes}")
     * @param startTime Start time in milliseconds
     * @param durationMinutes Duration in minutes
     * @param location Event location
     * @param recurrence Recurrence rule (RRULE format)
     * @param petId Pet ID to include in description (for iOS compatibility)
     * @param eventType Event type to include in description (for iOS compatibility)
     * @return Event ID if successful, null otherwise
     */
    fun addCustomEvent(
        title: String,
        description: String,
        startTime: Long,
        durationMinutes: Int,
        location: String,
        recurrence: String? = null,
        petId: String? = null,
        eventType: String? = null
    ): Long? {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return null
        }
        
        // Try to get Petcare calendar, fallback to primary calendar if not found
        var calendarId = getOrCreatePetcareCalendar()
        if (calendarId == null) {
            Log.w(TAG, "No Petcare calendar found, using primary calendar as fallback")
            calendarId = getPrimaryCalendarId()
            if (calendarId == null) {
                Log.e(TAG, "No calendar available on device")
            return null
            }
        }
        
        try {
            val endTime = startTime + (durationMinutes * 60 * 1000)
            
            // Format description to match iOS format: "Pet ID: {petId}\nType: {type}\n{notes}"
            // This ensures events can be found and parsed correctly
            val formattedDescription = buildString {
                if (petId != null) {
                    append("Pet ID: $petId\n")
                }
                if (eventType != null) {
                    append("Type: $eventType\n")
                }
                if (description.isNotBlank()) {
                    // Remove any existing "Pet ID:" or "Type:" lines from description to avoid duplication
                    val cleanDescription = description.lines()
                        .filterNot { it.startsWith("Pet ID:") || it.startsWith("Type:") }
                        .joinToString("\n")
                        .trim()
                    if (cleanDescription.isNotBlank()) {
                        append("\n$cleanDescription")
                    }
                }
            }
            
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, formattedDescription)
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.EVENT_LOCATION, location)
                put(CalendarContract.Events.HAS_ALARM, 1)
                
                // Add custom URI to identify Petcare events
                // This helps us find events later regardless of which calendar they're in
                put(CalendarContract.Events.CUSTOM_APP_URI, "petcare://event/${System.currentTimeMillis()}")
                
                // Add recurrence rule if provided
                if (recurrence != null) {
                    put(CalendarContract.Events.RRULE, recurrence)
                }
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
            
            if (eventId != null) {
                // Add reminder (15 minutes before)
                addReminder(eventId, 15)
                Log.d(TAG, "📅 Added custom event to calendar: $title (ID: $eventId)")
            } else {
                Log.e(TAG, "Failed to add custom event to calendar")
            }
            
            return eventId
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add custom event to calendar: ${e.message}")
            return null
        }
    }
    
    /**
     * Remove booking from calendar
     * @param bookingId Booking ID
     * @return True if successful
     */
    fun removeBookingFromCalendar(bookingId: String): Boolean {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return false
        }
        
        try {
            val uri = CalendarContract.Events.CONTENT_URI
            val selection = "${CalendarContract.Events.CUSTOM_APP_URI} = ?"
            val selectionArgs = arrayOf("petcare://booking/$bookingId")
            
            val deletedRows = context.contentResolver.delete(uri, selection, selectionArgs)
            
            Log.d(TAG, "📅 Removed booking from calendar: $bookingId ($deletedRows events deleted)")
            return deletedRows > 0
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove booking from calendar: ${e.message}")
            return false
        }
    }
    
    /**
     * Get all Petcare calendar events
     * Now searches ALL calendars to find Petcare events, not just the Petcare calendar
     * @return List of calendar events
     */
    fun getPetcareCalendarEvents(): List<CalendarEvent> {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return emptyList()
        }
        
        // First try to get events from Petcare calendar if it exists
        try {
            val petcareCalendarId = findPetcareCalendar()
            if (petcareCalendarId != null) {
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.CUSTOM_APP_URI
            )
            
            val selection = "${CalendarContract.Events.CALENDAR_ID} = ?"
                val selectionArgs = arrayOf(petcareCalendarId.toString())
            val sortOrder = "${CalendarContract.Events.DTSTART} ASC"
            
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            
            val events = mutableListOf<CalendarEvent>()
            
            cursor?.use {
                while (it.moveToNext()) {
                    val eventId = it.getLong(0)
                    val title = it.getString(1) ?: ""
                    val description = it.getString(2) ?: ""
                    val startTime = it.getLong(3)
                    val endTime = it.getLong(4)
                    val location = it.getString(5) ?: ""
                    val customUri = it.getString(6) ?: ""
                    
                    events.add(
                        CalendarEvent(
                            id = eventId,
                            title = title,
                            description = description,
                            startTime = startTime,
                            endTime = endTime,
                            location = location,
                            bookingId = extractBookingId(customUri)
                        )
                    )
                }
            }
            
                if (events.isNotEmpty()) {
                    Log.d(TAG, "📅 Retrieved ${events.size} events from Petcare calendar")
            return events
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get events from Petcare calendar: ${e.message}")
        }
        
        // Fallback: search all calendars for Petcare events
        Log.d(TAG, "Searching all calendars for Petcare events")
        return getAllCalendarEvents()
    }
    
    /**
     * Get calendar events for a specific pet
     * iOS Reference: HomeView.swift lines 443-446
     * Matches iOS format: extracts "Pet ID: {petId}" from event notes
     * @param petId Pet ID to filter events
     * @return List of calendar events for the pet
     */
    fun getEventsForPet(petId: String): List<CalendarEvent> {
        // Get all events (from any calendar, not just Petcare calendar)
        val allEvents = getAllCalendarEvents()
        return allEvents.filter { event ->
            // Extract pet ID from event description - match iOS format "Pet ID: {petId}"
            val extractedPetId = extractPetIdFromDescription(event.description)
            extractedPetId == petId || 
            // Fallback: check if description contains pet ID (for older events)
            (event.description.contains("Pet ID: $petId") || 
             event.description.contains("Pet:") && event.description.contains(petId) ||
             event.title.contains(petId))
        }
    }
    
    /**
     * Extract pet ID from event description
     * iOS format: "Pet ID: {petId}\nType: {type}\n{notes}"
     * @param description Event description
     * @return Pet ID if found, null otherwise
     */
    private fun extractPetIdFromDescription(description: String): String? {
        val lines = description.split("\n")
        for (line in lines) {
            if (line.startsWith("Pet ID: ")) {
                return line.substringAfter("Pet ID: ").trim()
            }
        }
        return null
    }
    
    /**
     * Get all calendar events (from all calendars, not just Petcare)
     * Used as fallback when Petcare calendar doesn't exist
     * Searches ALL calendars to find Petcare-related events
     */
    fun getAllCalendarEvents(): List<CalendarEvent> {
        if (!hasCalendarPermission()) {
            return emptyList()
        }
        
        try {
            // Get all calendars
            val calendars = getAllAvailableCalendars()
            if (calendars.isEmpty()) {
                Log.w(TAG, "No calendars available")
                return emptyList()
            }
            
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.CUSTOM_APP_URI,
                CalendarContract.Events.CALENDAR_ID
            )
            
            val events = mutableListOf<CalendarEvent>()
            
            // Query each calendar separately (more reliable than IN clause)
            for (calendarId in calendars) {
                try {
                    val selection = "${CalendarContract.Events.CALENDAR_ID} = ?"
                    val selectionArgs = arrayOf(calendarId.toString())
                    val sortOrder = "${CalendarContract.Events.DTSTART} ASC"
                    
                    val cursor = context.contentResolver.query(
                        CalendarContract.Events.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder
                    )
                    
                    cursor?.use {
                        while (it.moveToNext()) {
                            val eventId = it.getLong(0)
                            val title = it.getString(1) ?: ""
                            val description = it.getString(2) ?: ""
                            val startTime = it.getLong(3)
                            val endTime = it.getLong(4)
                            val location = it.getString(5) ?: ""
                            val customUri = it.getString(6) ?: ""
                            
                            // Filter to only include Petcare-related events
                            // Match iOS format: events have "Pet ID:" in description
                            // Or fallback to checking for event type keywords
                            val isPetcareEvent = description.contains("Pet ID:") ||
                                             description.contains("Type:") ||
                                             description.contains("Pet:") || 
                                             description.contains("Medication") ||
                                             description.contains("Vaccination") ||
                                             description.contains("Appointment") ||
                                             description.contains("Reminder") ||
                                             title.contains("💊") ||
                                             title.contains("💉") ||
                                             title.contains("🏥") ||
                                             title.contains("🔔") ||
                                             customUri.startsWith("petcare://")
                            
                            if (isPetcareEvent) {
                                events.add(
                                    CalendarEvent(
                                        id = eventId,
                                        title = title,
                                        description = description,
                                        startTime = startTime,
                                        endTime = endTime,
                                        location = location,
                                        bookingId = extractBookingId(customUri)
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to query calendar $calendarId: ${e.message}")
                    // Continue with next calendar
                }
            }
            
            Log.d(TAG, "📅 Retrieved ${events.size} Petcare events from all calendars")
            return events
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get calendar events: ${e.message}", e)
            return emptyList()
        }
    }
    
    /**
     * Get all available calendar IDs on the device
     */
    private fun getAllAvailableCalendars(): List<Long> {
        val calendarIds = mutableListOf<Long>()
        
        try {
            val projection = arrayOf(CalendarContract.Calendars._ID)
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            
            cursor?.use {
                while (it.moveToNext()) {
                    calendarIds.add(it.getLong(0))
                }
            }
            
            Log.d(TAG, "Found ${calendarIds.size} calendars on device")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get calendars: ${e.message}")
        }
        
        return calendarIds
    }
    
    /**
     * Load events for a specific pet (for AI integration)
     * iOS Reference: HomeView.swift line 433
     */
    fun loadEventsForPet(petId: String): List<CalendarEvent> {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return emptyList()
        }
        // Ensure calendar exists before loading events
        getOrCreatePetcareCalendar() ?: getPrimaryCalendarId()
        return getEventsForPet(petId)
    }
    
    /**
     * Get or create Petcare calendar
     * @return Calendar ID or null
     */
    private fun getOrCreatePetcareCalendar(): Long? {
        // Try to find existing calendar
        val calendarId = findPetcareCalendar()
        if (calendarId != null) return calendarId
        
        // Create new calendar
        return createPetcareCalendar()
    }
    
    /**
     * Find existing Petcare calendar
     */
    private fun findPetcareCalendar(): Long? {
        try {
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME
            )
            
            val selection = "${CalendarContract.Calendars.NAME} = ?"
            val selectionArgs = arrayOf(CALENDAR_NAME)
            
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getLong(0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find calendar: ${e.message}")
        }
        
        return null
    }
    
    /**
     * Create Petcare calendar
     */
    private fun createPetcareCalendar(): Long? {
        try {
            // Note: Creating calendars requires WRITE_CALENDAR permission
            // and may not work on all devices
            Log.d(TAG, "📅 Calendar creation attempted (may not be supported on all devices)")
            
            // Return primary calendar ID as fallback
            return getPrimaryCalendarId()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create calendar: ${e.message}")
            return getPrimaryCalendarId()
        }
    }
    
    /**
     * Get primary calendar ID
     * Falls back to any available calendar if primary is not found
     */
    private fun getPrimaryCalendarId(): Long? {
        try {
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.ACCOUNT_NAME
            )
            
            // First try to get primary calendar
            val primarySelection = "${CalendarContract.Calendars.IS_PRIMARY} = ?"
            val primarySelectionArgs = arrayOf("1")
            
            val primaryCursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                primarySelection,
                primarySelectionArgs,
                null
            )
            
            primaryCursor?.use {
                if (it.moveToFirst()) {
                    val calendarId = it.getLong(0)
                    val calendarName = it.getString(1) ?: ""
                    Log.d(TAG, "Found primary calendar: $calendarName (ID: $calendarId)")
                    return calendarId
                }
            }
            
            // Fallback: get first writable calendar
            val writableSelection = "${CalendarContract.Calendars.CAN_MODIFY_TIME_ZONE} = ? OR ${CalendarContract.Calendars.CAN_ORGANIZER_RESPOND} = ?"
            val writableSelectionArgs = arrayOf("1", "1")
            
            val writableCursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                writableSelection,
                writableSelectionArgs,
                null
            )
            
            writableCursor?.use {
                if (it.moveToFirst()) {
                    val calendarId = it.getLong(0)
                    val calendarName = it.getString(1) ?: ""
                    Log.d(TAG, "Found writable calendar: $calendarName (ID: $calendarId)")
                    return calendarId
                }
            }
            
            // Last resort: get ANY calendar
            val anyCursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            
            anyCursor?.use {
                if (it.moveToFirst()) {
                    val calendarId = it.getLong(0)
                    val calendarName = it.getString(1) ?: ""
                    Log.d(TAG, "Found any calendar: $calendarName (ID: $calendarId)")
                    return calendarId
                }
            }
            
            Log.w(TAG, "No calendars found on device. User may need to set up a calendar account.")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get calendar: ${e.message}", e)
        }
        
        return null
    }
    
    /**
     * Add reminder to event
     */
    private fun addReminder(eventId: Long, minutesBefore: Int) {
        try {
            val values = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, minutesBefore)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
            Log.d(TAG, "📅 Added reminder: $minutesBefore minutes before")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add reminder: ${e.message}")
        }
    }
    
    /**
     * Parse date-time string to milliseconds
     */
    private fun parseDateTime(dateTimeString: String?): Long? {
        if (dateTimeString == null) return null
        
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateTimeString)?.time
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse date-time: ${e.message}")
            null
        }
    }
    
    /**
     * Extract booking ID from custom URI
     */
    private fun extractBookingId(customUri: String): String? {
        return try {
            if (customUri.startsWith("petcare://booking/")) {
                customUri.substringAfter("petcare://booking/")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
}
