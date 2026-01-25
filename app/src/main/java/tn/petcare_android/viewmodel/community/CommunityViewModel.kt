package tn.petcare_android.viewmodel.community

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.model.community.*
import java.io.File

class CommunityViewModel : ViewModel() {
    
    private val communityApi = RetrofitInstance.communityApi
    
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress: StateFlow<Float?> = _uploadProgress.asStateFlow()
    
    private var currentPage = 1
    private var hasMorePages = true
    
    init {
        loadPosts()
    }
    
    fun loadPosts(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _isRefreshing.value = true
                currentPage = 1
                hasMorePages = true
            } else {
                _isLoading.value = true
            }
            _error.value = null
            
            try {
                val response = communityApi.getPosts(page = currentPage, limit = 10)
                
                if (response.isSuccessful) {
                    val postsResponse = response.body()
                    postsResponse?.let {
                        if (refresh) {
                            _posts.value = it.posts
                        } else {
                            // Filter out duplicates by ID
                            val existingIds = _posts.value.map { post -> post.id }.toSet()
                            val newPosts = it.posts.filter { post -> !existingIds.contains(post.id) }
                            _posts.value = _posts.value + newPosts
                        }
                        hasMorePages = currentPage < it.totalPages
                    }
                } else {
                    _error.value = "Failed to load posts: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load posts"
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }
    
    fun loadMorePosts() {
        if (!_isLoading.value && hasMorePages) {
            currentPage++
            loadPosts()
        }
    }
    
    fun createPost(imageFile: File, caption: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _uploadProgress.value = 0f
            _error.value = null
            
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData(
                    "petImage",
                    imageFile.name,
                    requestFile
                )
                
                _uploadProgress.value = 0.5f
                
                val response = communityApi.createPost(
                    caption = caption,
                    petImage = imagePart
                )
                
                _uploadProgress.value = 1f
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        // Add new post to the beginning of the list
                        _posts.value = listOf(it.post) + _posts.value
                    }
                } else {
                    _error.value = "Failed to create post: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create post"
            } finally {
                _isLoading.value = false
                _uploadProgress.value = null
            }
        }
    }
    
    fun reactToPost(postId: String, reactionType: ReactionType) {
        viewModelScope.launch {
            try {
                val post = _posts.value.find { it.id == postId } ?: return@launch
                
                // Check if user already has the same reaction
                val alreadyReacted = post.userReaction == reactionType.value
                
                val response = if (alreadyReacted) {
                    // Remove reaction if clicking the same one
                    communityApi.removeReaction(postId, reactionType.value)
                } else {
                    // Add new reaction (will replace old one on backend)
                    communityApi.reactToPost(
                        postId,
                        ReactRequest(reactionType.value)
                    )
                }
                
                if (response.isSuccessful) {
                    response.body()?.let { reactResponse ->
                        // Update the post in the list
                        _posts.value = _posts.value.map {
                            if (it.id == postId) reactResponse.post else it
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to react to post"
            }
        }
    }
    
    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                val response = communityApi.deletePost(postId)
                
                if (response.isSuccessful) {
                    // Remove post from the list
                    _posts.value = _posts.value.filter { it.id != postId }
                } else {
                    _error.value = "Failed to delete post: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete post"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun loadMyPosts(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _isRefreshing.value = true
                currentPage = 1
                hasMorePages = true
            } else {
                _isLoading.value = true
            }
            
            try {
                val response = communityApi.getMyPosts(
                    page = currentPage,
                    limit = 10
                )
                
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        if (refresh) {
                            _posts.value = data.posts
                        } else {
                            // Filter out duplicates by ID
                            val existingIds = _posts.value.map { post -> post.id }.toSet()
                            val newPosts = data.posts.filter { post -> !existingIds.contains(post.id) }
                            _posts.value = _posts.value + newPosts
                        }
                        
                        hasMorePages = currentPage < data.totalPages
                        currentPage++
                    }
                } else {
                    _error.value = "Failed to load posts: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }
    
    fun loadMoreMyPosts() {
        if (!_isLoading.value && hasMorePages) {
            loadMyPosts()
        }
    }
    
    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            try {
                val response = communityApi.addComment(
                    postId,
                    AddCommentRequest(text)
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { commentResponse ->
                        // Update the post in the list with new comment
                        _posts.value = _posts.value.map {
                            if (it.id == postId) commentResponse.post else it
                        }
                    }
                } else {
                    _error.value = "Failed to add comment: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add comment"
            }
        }
    }
    
    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            try {
                val response = communityApi.deleteComment(postId, commentId)
                
                if (response.isSuccessful) {
                    response.body()?.let { commentResponse ->
                        // Update the post in the list with comment removed
                        _posts.value = _posts.value.map {
                            if (it.id == postId) commentResponse.post else it
                        }
                    }
                } else {
                    _error.value = "Failed to delete comment: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete comment"
            }
        }
    }
    
    fun reportPost(postId: String) {
        viewModelScope.launch {
            try {
                val response = communityApi.reportPost(postId)
                
                if (response.isSuccessful) {
                    response.body()?.let { reportResponse ->
                        if (reportResponse.deleted) {
                            // Remove post from list if it was deleted
                            _posts.value = _posts.value.filter { it.id != postId }
                        }
                        _error.value = reportResponse.message
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    _error.value = "Failed to report post: $errorMsg"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to report post"
            }
        }
    }
}
