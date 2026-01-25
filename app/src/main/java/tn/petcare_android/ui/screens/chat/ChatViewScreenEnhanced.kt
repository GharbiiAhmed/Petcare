package tn.petcare_android.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.chat.Message
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.util.AudioPlayer
import tn.petcare_android.util.AudioRecorder
import tn.petcare_android.util.formatDuration
import tn.petcare_android.viewmodel.chat.ChatViewModel

/**
 * Enhanced ChatViewScreen with Audio Messages
 * iOS Reference: ChatView.swift with audio recording/playback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatViewScreenEnhanced(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference,
    recipientId: String,
    recipientName: String,
    currentUserId: String,
    conversationId: String? = null,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var messageText by remember { mutableStateOf("") }
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    // Audio recording state
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    var showRecordingDialog by remember { mutableStateOf(false) }
    
    val audioRecorder = remember { AudioRecorder(context) }
    val audioPlayer = remember { AudioPlayer(context) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showRecordingDialog = true
        }
    }
    
    // Load messages
    LaunchedEffect(conversationId) {
        conversationId?.let {
            viewModel.loadMessages(it)
        }
    }
    
    // Auto scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    // Recording timer
    LaunchedEffect(isRecording) {
            while (isRecording) {
                delay(1000)
                recordingDuration++
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.release()
            audioPlayer.release()
            viewModel.leaveCurrentConversation()
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = recipientName,
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController,
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading && messages.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = VetCanyon
                        )
                    }
                    messages.isEmpty() -> {
                        Text(
                            text = "No messages yet. Start the conversation!",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages) { message ->
                                MessageBubbleEnhanced(
                                    message = message,
                                    isFromCurrentUser = message.normalizedSenderId == currentUserId,
                                    audioPlayer = audioPlayer,
                                    onEdit = {
                                        editingMessageId = message.normalizedId
                                        messageText = message.content
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        coroutineScope.launch {
                                            viewModel.deleteMessage(message.normalizedId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Message Input with Audio
            MessageInputEnhanced(
                text = messageText,
                onTextChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank() && conversationId != null) {
                        coroutineScope.launch {
                            val success = viewModel.sendMessage(conversationId, messageText.trim())
                            if (success) {
                                messageText = ""
                            }
                        }
                    }
                },
                onAudioRecord = {
                    // Check permission
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        showRecordingDialog = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                enabled = conversationId != null
            )
        }
    }
    
    // Recording Dialog
    if (showRecordingDialog) {
        RecordingDialog(
            isRecording = isRecording,
            duration = recordingDuration,
            onStartRecording = {
                audioRecorder.startRecording()
                isRecording = true
                recordingDuration = 0
            },
            onStopRecording = {
                val audioFile = audioRecorder.stopRecording()
                isRecording = false
                showRecordingDialog = false
                recordingDuration = 0
                
                // Upload audio file and send message
                if (audioFile != null && conversationId != null) {
                    coroutineScope.launch {
                        viewModel.uploadAudioMessage(
                            recipientId = recipientId,
                            conversationId = conversationId,
                            audioFile = audioFile
                        )
                    }
                }
            },
            onCancel = {
                if (isRecording) {
                    audioRecorder.cancelRecording()
                    isRecording = false
                }
                showRecordingDialog = false
                recordingDuration = 0
            }
        )
    }
    
    // Edit Message Dialog
    if (showEditDialog && editingMessageId != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                editingMessageId = null
                messageText = ""
            },
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Edit your message...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VetCanyon
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editingMessageId?.let { id ->
                            coroutineScope.launch {
                                viewModel.editMessage(id, messageText.trim())
                                showEditDialog = false
                                editingMessageId = null
                                messageText = ""
                            }
                        }
                    }
                ) {
                    Text("Save", color = VetCanyon)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        editingMessageId = null
                        messageText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MessageBubbleEnhanced(
    message: Message,
    isFromCurrentUser: Boolean,
    audioPlayer: AudioPlayer,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var isPlayingThisAudio by remember { mutableStateOf(false) }
    val isPlaying by audioPlayer.isPlaying.collectAsState()
    val currentPosition by audioPlayer.currentPosition.collectAsState()
    val duration by audioPlayer.duration.collectAsState()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromCurrentUser) {
            Spacer(modifier = Modifier.width(60.dp))
        }
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isFromCurrentUser) VetCanyon else CardBackground,
            modifier = Modifier.widthIn(max = 300.dp),
            onClick = { if (isFromCurrentUser) showMenu = true }
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Audio message
                    if (message.audioURL != null) {
                        AudioMessageContent(
                            audioUrl = message.audioURL,
                            messageId = message.normalizedId,
                            isPlaying = isPlayingThisAudio && isPlaying,
                            currentPosition = currentPosition,
                            duration = duration,
                            isFromCurrentUser = isFromCurrentUser,
                            onPlayPause = {
                                message.audioURL?.let { url ->
                                    if (isPlayingThisAudio && isPlaying) {
                                        audioPlayer.pause()
                                        isPlayingThisAudio = false
                                    } else {
                                        audioPlayer.playFromUrl(url, message.normalizedId)
                                        isPlayingThisAudio = true
                                    }
                                }
                            }
                        )
                    } else {
                        // Text message
                        Text(
                            text = message.content,
                            fontSize = 14.sp,
                            color = if (isFromCurrentUser) Color.White else TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                    
                    if (message.isEdited) {
                        Text(
                            text = "Edited",
                            fontSize = 11.sp,
                            color = if (isFromCurrentUser) Color.White.copy(alpha = 0.7f) else TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Menu
                if (isFromCurrentUser) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (message.audioURL == null) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        )
                    }
                }
            }
        }
        
        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(60.dp))
        }
    }
}

@Composable
private fun AudioMessageContent(
    audioUrl: String,
    messageId: String,
    isPlaying: Boolean,
    currentPosition: Int,
    duration: Int,
    isFromCurrentUser: Boolean,
    onPlayPause: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Stop" else "Play",
                tint = if (isFromCurrentUser) Color.White else VetCanyon
            )
        }
        
        // Waveform / Progress
        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (isFromCurrentUser) Color.White else VetCanyon,
                trackColor = if (isFromCurrentUser) Color.White.copy(alpha = 0.3f) else VetCanyon.copy(alpha = 0.3f)
            )
            
            Text(
                text = if (isPlaying) formatDuration(currentPosition) else formatDuration(duration),
                fontSize = 12.sp,
                color = if (isFromCurrentUser) Color.White.copy(alpha = 0.8f) else TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Audio",
            tint = if (isFromCurrentUser) Color.White.copy(alpha = 0.7f) else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MessageInputEnhanced(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAudioRecord: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Audio button
            IconButton(
                onClick = onAudioRecord,
                enabled = enabled,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (enabled) VetCanyon.copy(alpha = 0.1f) else VetCanyon.copy(alpha = 0.05f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Record Audio",
                    tint = if (enabled) VetCanyon else VetCanyon.copy(alpha = 0.4f)
                )
            }
            
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VetInputBackground,
                    unfocusedContainerColor = VetInputBackground,
                    focusedBorderColor = VetCanyon,
                    unfocusedBorderColor = VetStroke
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
                enabled = enabled
            )
            
            IconButton(
                onClick = onSend,
                enabled = enabled && text.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (enabled && text.isNotBlank()) VetCanyon else VetCanyon.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun RecordingDialog(
    isRecording: Boolean,
    duration: Long,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, "Recording", tint = if (isRecording) Color.Red else VetCanyon)
                Text(if (isRecording) "Recording..." else "Record Audio")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRecording) {
                    // Animated recording indicator
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.Red.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
            ) {
                Icon(
                            Icons.Default.PlayArrow,
                            "Recording",
                            tint = Color.Red,
                            modifier = Modifier.size(40.dp)
                )
                    }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                        text = formatDuration((duration * 1000).toInt()),
                    fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                } else {
                    Text("Tap the microphone to start recording")
                }
            }
        },
        confirmButton = {
            if (isRecording) {
                Button(
                    onClick = onStopRecording,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Check, "Stop")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Done")
                }
            } else {
                IconButton(
                    onClick = onStartRecording,
                    modifier = Modifier
                        .size(64.dp)
                        .background(VetCanyon, CircleShape)
                ) {
                    Icon(Icons.Default.PlayArrow, "Start", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(if (isRecording) "Cancel" else "Close")
            }
        }
    )
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Spacer(modifier = Modifier.width(60.dp))
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBackground,
            modifier = Modifier.widthIn(max = 100.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    var alpha by remember { mutableStateOf(0.3f) }
                    
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(index * 200L)
                            alpha = 1f
                            delay(600)
                            alpha = 0.3f
                            delay(200)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(TextSecondary.copy(alpha = alpha), CircleShape)
                    )
                }
            }
        }
    }
}
