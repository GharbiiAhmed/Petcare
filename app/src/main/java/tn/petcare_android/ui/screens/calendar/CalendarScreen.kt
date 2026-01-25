package tn.petcare_android.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.util.JwtDecoder
import tn.petcare_android.ui.theme.*

data class CalendarEvent(
    val icon: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val borderColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavHostController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    
    // Get user role
    var userRole by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val token = tokenManager.getAccessToken().firstOrNull()
        userRole = token?.let { JwtDecoder.getRoleFromToken(it) }
    }
    
    // TODO: Use CalendarViewModel to fetch events from backend API based on role
    // Example: val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModelFactory(LocalContext.current))
    // val events by viewModel.events.collectAsState()
    val events = emptyList<CalendarEvent>() // Replace with dynamic data from backend API
    
    // Role-specific title
    val calendarTitle = when (userRole?.lowercase()) {
        "vet" -> "Appointments Calendar"
        "sitter" -> "Sitting Schedule"
        "trainer" -> "Training Schedule"
        "salon" -> "Appointments Calendar"
        "admin" -> "Calendar"
        else -> "Calendar"
    }

    Scaffold(
        topBar = { CalendarTopBar(navController = navController, title = calendarTitle) },
        containerColor = PageBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Navigate to add calendar event
                    navController.navigate("add_calendar_event")
                },
                containerColor = OrangeAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Event")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Upcoming Events",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(events) { event ->
                EventCard(event)
            }

            if (events.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📅",
                            fontSize = 64.sp
                        )
                        Text(
                            text = "No events scheduled",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap the + button to add a new event",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(navController: NavHostController, title: String = "Calendar") {
    TopAppBar(
        title = {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                color = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate("conversations") }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = OrangeAccent
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBackground
        )
    )
}

@Composable
private fun EventCard(event: CalendarEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left border indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(event.borderColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Event icon
            Text(text = event.icon, fontSize = 24.sp)

            Spacer(modifier = Modifier.width(12.dp))

            // Event details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (event.subtitle.isNotEmpty()) {
                    Text(
                        text = event.subtitle,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Replaced unavailable icon with emoji to avoid unresolved reference
                    Text(text = "⏰", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        text = event.time,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
