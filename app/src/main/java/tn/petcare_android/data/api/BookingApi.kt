package tn.petcare_android.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.Query
import tn.petcare_android.data.model.booking.Booking
import tn.petcare_android.data.model.booking.CreateBookingRequest
import tn.petcare_android.data.model.booking.UpdateBookingRequest

interface BookingApi {
    @GET("/bookings")
    suspend fun getBookings(
        @Query("role") role: String? = null,
        @Query("status") status: String? = null
    ): List<Booking>

    @GET("/bookings/{id}")
    suspend fun getBookingById(@Path("id") bookingId: String): Booking

    @POST("/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): Booking

    @PUT("/bookings/{id}")
    suspend fun updateBooking(
        @Path("id") bookingId: String,
        @Body request: UpdateBookingRequest
    ): Booking

    @DELETE("/bookings/{id}")
    suspend fun deleteBooking(
        @Path("id") bookingId: String
    )
}
