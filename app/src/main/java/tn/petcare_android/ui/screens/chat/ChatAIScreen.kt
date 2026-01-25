package tn.petcare_android.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Base64
import java.io.InputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.ai.AIChatMessage
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.ai.AIChatViewModel
import tn.petcare_android.viewmodel.ai.AIChatViewModelFactory
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAIScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: AIChatViewModel = viewModel(factory = AIChatViewModelFactory(context))

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var imageTooLargeMessage by remember { mutableStateOf<String?>(null) }

    // Helper: compress/resize image from Uri to Base64 string under target byte size.
// Returns Base64 string if successful, or null if it couldn't be reduced.
    fun tryCompressImageToBase64(context: android.content.Context, uri: Uri, targetBytes: Int = 1_000_000, maxDim: Int = 1024): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
// Decode bounds first to compute scale
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            inputStream.use { BitmapFactory.decodeStream(it, null, options) }

            var actualWidth = options.outWidth
            var actualHeight = options.outHeight
            if (actualWidth <= 0 || actualHeight <= 0) return null

            val maxSide = maxOf(actualWidth, actualHeight)
            var scale = 1.0f
            if (maxSide > maxDim) {
                scale = maxDim.toFloat() / maxSide.toFloat()
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = 1 }
            var bitmap: Bitmap? = null
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val original = BitmapFactory.decodeStream(stream)
                if (original == null) return null
                if (scale < 1.0f) {
                    val newW = (original.width * scale).toInt()
                    val newH = (original.height * scale).toInt()
                    bitmap = Bitmap.createScaledBitmap(original, newW, newH, true)
                    if (bitmap !== original) original.recycle()
                } else {
                    bitmap = original
                }
            }

            bitmap ?: return null

// Try compressing with decreasing JPEG quality until under targetBytes or quality too low
            var quality = 90
            var resultBytes: ByteArray
            do {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                resultBytes = baos.toByteArray()
                baos.close()
                if (resultBytes.size <= targetBytes || quality <= 40) break
                quality -= 10
            } while (quality > 30)

// Clean up bitmap
            bitmap.recycle()

            if (resultBytes.size > targetBytes) return null

            Base64.encodeToString(resultBytes, Base64.NO_WRAP)
        } catch (t: Throwable) {
            null
        }
    }

// Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
// Read stream and convert to Base64
            try {
// Attempt to compress/resize the image to stay under target size
                val compressed = tryCompressImageToBase64(context, uri, targetBytes = 1_000_000, maxDim = 1024)
                if (compressed != null) {
                    selectedImageBase64 = compressed
                    imageTooLargeMessage = null
                } else {
// compression failed or couldn't reduce below target — keep preview but warn user
                    selectedImageBase64 = null
                    imageTooLargeMessage = "Image is too large or unsupported. It will not be sent."
                }
            } catch (_: Exception) {
                selectedImageBase64 = null
            }
        } else {
            selectedImageUri = null
            selectedImageBase64 = null
        }
    }




// Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100) // Small delay for UI update
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { ChatTopBar(navController, onClearHistory = { showClearConfirm = true }) },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message)
                }

// Show loading indicator when AI is responding
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .padding(end = 48.dp),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = AIBubbleColor
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = OrangeAccent
                                    )
                                    Text(
                                        "AI is thinking...",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

// Show error message if any
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

// Preview selected image (if any) - moved here so it's visible above the input area
            selectedImageUri?.let { _ ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Image attached",
                            color = TextPrimary
                        )
                        imageTooLargeMessage?.let { msg ->
                            Text(
                                text = msg,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                    TextButton(onClick = {
// clear selection
                        selectedImageUri = null
                        selectedImageBase64 = null
                    }) {
                        Text("Remove")
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your question...", color = TextSecondary) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    maxLines = 3,
                    enabled = !isLoading
                )

// Attach image button
                IconButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.size(48.dp),
                    enabled = !isLoading
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Attach image")
                }

                IconButton(
                    onClick = {
                        if ((messageText.isNotBlank() || selectedImageBase64 != null) && !isLoading) {
                            viewModel.sendMessage(messageText, selectedImageBase64)
                            messageText = ""
                            selectedImageUri = null
                            selectedImageBase64 = null
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(OrangeAccent, RoundedCornerShape(24.dp)),
                    enabled = !isLoading && (messageText.isNotBlank() || selectedImageBase64 != null)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear conversation") },
            text = { Text("Are you sure you want to delete your conversation history? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    viewModel.clearHistoryServer()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(navController: NavHostController, onClearHistory: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "AI Assistant",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Text(
                    "Online",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = { onClearHistory() }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear history", tint = TextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBackground
        )
    )
}

@Composable
private fun MessageBubble(message: AIChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(
                    start = if (message.isFromUser) 48.dp else 0.dp,
                    end = if (message.isFromUser) 0.dp else 48.dp
                ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) UserBubbleColor else AIBubbleColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (message.isFromUser) Color.White else TextPrimary
                )

                // Show image if message has an imageUrl
                message.imageUrl?.let { url ->
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = "Message image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 300.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
        }
    }
}