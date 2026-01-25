package tn.petcare_android.ui.screens.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tn.petcare_android.data.model.booking.UpdateBookingRequest
import tn.petcare_android.viewmodel.booking.BookingViewModel

@Composable
fun BookingUpdateScreen(viewModel: BookingViewModel, bookingId: String, onBookingUpdated: () -> Unit) {
    var status by remember { mutableStateOf("") }
    var rejectionReason by remember { mutableStateOf("") }
    var cancellationReason by remember { mutableStateOf("") }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Update Booking", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status") })
        OutlinedTextField(value = rejectionReason, onValueChange = { rejectionReason = it }, label = { Text("Rejection Reason") })
        OutlinedTextField(value = cancellationReason, onValueChange = { cancellationReason = it }, label = { Text("Cancellation Reason") })
        Spacer(modifier = Modifier.height(16.dp))
        if (loading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                val request = UpdateBookingRequest(
                    status = if (status.isNotBlank()) status else null,
                    rejectionReason = if (rejectionReason.isNotBlank()) rejectionReason else null,
                    cancellationReason = if (cancellationReason.isNotBlank()) cancellationReason else null
                )
                viewModel.updateBooking(bookingId, request)
                onBookingUpdated()
            }) {
                Text("Update Booking")
            }
        }
        if (error != null) {
            Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
        }
    }
}
