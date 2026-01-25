package tn.petcare_android.data.repository

import tn.petcare_android.data.api.*
import tn.petcare_android.data.model.trainers.Trainer
import tn.petcare_android.data.model.trainers.TrainerBooking
import javax.inject.Inject

class TrainersRepository @Inject constructor(
    private val trainersApi: TrainersApi
) {

    suspend fun getAllTrainers() = runCatching {
        trainersApi.getAllTrainers()
    }

    suspend fun getTrainer(trainerId: String) = runCatching {
        trainersApi.getTrainer(trainerId)
    }

    suspend fun createBooking(trainerId: String, request: CreateTrainerBookingRequest) = runCatching {
        trainersApi.createTrainerBooking(trainerId, request)
    }

    suspend fun getTrainerBookings(trainerId: String) = runCatching {
        trainersApi.getTrainerBookings(trainerId)
    }

    suspend fun getOwnerBookings(ownerId: String) = runCatching {
        trainersApi.getOwnerBookings(ownerId)
    }

    suspend fun getBooking(bookingId: String) = runCatching {
        trainersApi.getBooking(bookingId)
    }

    suspend fun updateBooking(bookingId: String, request: UpdateTrainerBookingRequest) = runCatching {
        trainersApi.updateBooking(bookingId, request)
    }

    suspend fun confirmBooking(bookingId: String) = runCatching {
        trainersApi.confirmBooking(bookingId)
    }

    suspend fun rejectBooking(bookingId: String, request: RejectBookingRequest) = runCatching {
        trainersApi.rejectBooking(bookingId, request)
    }

    suspend fun cancelBooking(bookingId: String, request: CancelBookingRequest) = runCatching {
        trainersApi.cancelBooking(bookingId, request)
    }

    suspend fun completeBooking(bookingId: String) = runCatching {
        trainersApi.completeBooking(bookingId)
    }
}
